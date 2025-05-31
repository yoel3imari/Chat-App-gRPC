
package com.adia.auth.grpc;

import com.adia.auth.*;
import com.adia.auth.Empty;
import com.adia.user.User;
import com.adia.auth.entity.RefreshToken;
import com.adia.auth.service.RefreshTokenService;
import com.adia.auth.util.JwtUtil;
import com.adia.user.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

@GrpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    public AuthServiceImpl(RefreshTokenService refreshTokenService, JwtUtil jwtUtil) {
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<AuthResponse> responseObserver) {

//        System.out.println(request.toString());

        try {
            CreateUserRequest userRequest = CreateUserRequest.newBuilder()
                    .setEmail(request.getEmail())
                    .setUsername(request.getUsername())
                    .setPassword(request.getPassword())
                    .setIsActivated(true)
                    .setIsEmailVerified(false)
                    .setIsAdmin(false)
                    .setIsSuspended(false)
                    .build();

            // user service is going to check if user already exist, then save it
            UserResponse userResponse = userService.createUser(userRequest);

            if (!userResponse.getSuccess()) {
                responseObserver.onNext(AuthResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("User creation failed: " + userResponse.getMessage())
                        .build());
                responseObserver.onCompleted();
                return;
            }

            // 2. Generate access + refresh tokens
            User user = User.newBuilder()
                    .setId(userResponse.getUser().getId())
                    .setEmail(userResponse.getUser().getEmail())
                    .setUsername(userResponse.getUser().getUsername())
                    .setIsAdmin(userResponse.getUser().getIsAdmin())
                    .setIsEmailVerified(userResponse.getUser().getIsEmailVerified())
                    .setIsActivated(userResponse.getUser().getIsActivated())
                    .setIsSuspended(userResponse.getUser().getIsSuspended())
                    .build();

            // 4. Create and persist refresh token
            String accessToken = jwtUtil.generateToken(user.getEmail());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            AuthResponse response = AuthResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User created successfully")
                    .setAccessToken(accessToken)
                    .setRefreshToken(refreshToken.getToken())
                    .setExpiresIn(refreshToken.getExpiryDate().getSecond())
                    .setUser(user)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void login(LoginRequest request, StreamObserver<AuthResponse> responseObserver) {
        try {
            String email = request.getEmail();
            String password = request.getPassword();

            // check if user exists with email

            UserResponse userResponse = userService.getUserByEmail(
                    GetUserByEmailRequest.newBuilder().setEmail(email).build()
            );

            if (!userResponse.getSuccess()) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("This account does not exist")
                        .asRuntimeException());
                return;
            }

            User user = userResponse.getUser();

            // if not active
            if (!userResponse.getUser().getIsActivated()) {
                responseObserver.onError(Status.PERMISSION_DENIED
                        .withDescription("This account is not activated")
                        .asRuntimeException());
                return;
            }

            // if is suspended
            if (userResponse.getUser().getIsSuspended()) {
                responseObserver.onError(Status.PERMISSION_DENIED
                        .withDescription("This account is suspended")
                        .asRuntimeException());
                return;
            }

            // if yes, validate password
            VPasswordRes vpres = userService.verifyPassword(
                    VPasswordReq.newBuilder()
                            .setEmail(user.getEmail())
                            .setPassword(request.getPassword())
                            .build());

            if (!vpres.getSuccess()) {
                responseObserver.onError(Status.PERMISSION_DENIED
                        .withDescription("wrong password")
                        .asRuntimeException());
                return;
            }

            // if valide return access + refresh tokens
            String accessToken = jwtUtil.generateToken(user.getEmail());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userResponse.getUser());

            AuthResponse response = AuthResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User logged in!")
                    .setAccessToken(accessToken)
                    .setRefreshToken(refreshToken.getToken())
                    .setExpiresIn(refreshToken.getExpiryDate().getSecond())
                    .setUser(user)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Error during user login", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<Empty> responseObserver) {
        try {
            long id = request.getUserId();
            refreshTokenService.deleteByUserId(id);

            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error during user logout", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void refreshToken(RefreshTokenRequest request, StreamObserver<AuthResponse> responseObserver) {
        try {
            String requestToken = request.getRefreshToken();

            Optional<RefreshToken> refreshTokenOpt = refreshTokenService.findByToken(requestToken);

            if (refreshTokenOpt.isEmpty() || refreshTokenService.isTokenExpired(refreshTokenOpt.get())) {
                responseObserver.onNext(AuthResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Invalid or expired refresh token")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            RefreshToken refreshToken = refreshTokenOpt.get();
            UserResponse userResponse = userService.getUser(
                    GetUserRequest.newBuilder()
                            .setId(refreshToken.getUserId())
                            .build());

            if (!userResponse.getSuccess()) {
                responseObserver.onNext(AuthResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("User not found")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            String newAccessToken = jwtUtil.generateToken(userResponse.getUser().getEmail()); // or ID
            String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken); // optional

            User user = User.newBuilder()
                    .setId(userResponse.getUser().getId())
                    .setEmail(userResponse.getUser().getEmail())
                    .setUsername(userResponse.getUser().getUsername())
                    .setIsAdmin(userResponse.getUser().getIsAdmin())
                    .setIsEmailVerified(userResponse.getUser().getIsEmailVerified())
                    .setIsActivated(userResponse.getUser().getIsActivated())
                    .setIsSuspended(userResponse.getUser().getIsSuspended())
                    .build();

            AuthResponse response = AuthResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Access token refreshed successfully")
                    .setAccessToken(newAccessToken)
                    .setRefreshToken(newRefreshToken)
                    .setUser(user)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Error during user registration", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
