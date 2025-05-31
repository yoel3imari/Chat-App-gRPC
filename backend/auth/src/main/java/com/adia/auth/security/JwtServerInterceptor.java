package com.adia.auth.security;


import com.adia.auth.util.JwtUtil;
import com.adia.user.GetUserByEmailRequest;
import com.adia.user.User;
import com.adia.user.UserResponse;
import com.adia.user.UserServiceGrpc;
import io.grpc.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

        
@Component
public class JwtServerInterceptor implements ServerInterceptor {

    private final JwtUtil jwtUtil;
    private final Set<String> publicMethods;

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    public JwtServerInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        
        // Initialize public methods that don't require authentication
        this.publicMethods = new HashSet<>(Arrays.asList(
            "auth.AuthService/Login",
            "auth.AuthService/Register",
            "auth.AuthService/RefreshToken"
        ));
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        // Get the full method name (service + method)
        String fullMethodName = call.getMethodDescriptor().getFullMethodName();
        
        // Skip authentication for public methods
        if (publicMethods.contains(fullMethodName)) {
            return next.startCall(call, headers);
        }
        
        try {
            String authHeader = headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new AuthenticationException("Missing or invalid Authorization header");
            }

            String jwt = authHeader.substring(7);
            String email = jwtUtil.extractSubject(jwt);
            
            if (email == null) {
                throw new AuthenticationException("Invalid token: missing subject");
            }
            
            // Load user details and verify token is valid for this user
            // UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UserResponse response = userService.getUserByEmail(
                    GetUserByEmailRequest.newBuilder()
                            .setEmail(email)
                            .build());

            if (!response.getSuccess()) {
                throw new AuthenticationException("Invalid token: No user with provided token");
            }

            User userDetails = response.getUser();

            if (!jwtUtil.isTokenValid(jwt, userDetails.getEmail())) {
                throw new AuthenticationException("Invalid token for user");
            }
            
            // Authentication successful, proceed with call
            return next.startCall(call, headers);
            
        } catch (AuthenticationException e) {
            call.close(Status.UNAUTHENTICATED.withDescription(e.getMessage()), headers);
            return new ServerCall.Listener<>() {};
        } catch (Exception e) {
            call.close(Status.INTERNAL.withDescription("Internal authentication error"), headers);
            return new ServerCall.Listener<>() {};
        }
    }
    
    // Simple exception class for authentication failures
    private static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
