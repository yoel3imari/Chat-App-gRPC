package com.adia.admin.grpc;

import com.adia.admin.*;
import com.adia.user.GetUserRequest;

import com.adia.admin.AdminServiceGrpc;
import com.adia.admin.DltConversationRequest;
import com.adia.admin.MessageActionRequest;
import com.adia.admin.MessageActionResponse;
import com.adia.admin.UserActionRequest;
import com.adia.admin.UserActionResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.logging.Logger;

@GrpcService
public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {
    @GrpcClient("user-service")
    private com.adia.user.UserServiceGrpc.UserServiceBlockingStub userService;

    @Override
    public void deleteConversation(DltConversationRequest request,
                                   StreamObserver<com.adia.admin.DltConversationResponse> responseObserver) {

    }

    @Override
    public void deleteMessage(MessageActionRequest request, StreamObserver<MessageActionResponse> responseObserver) {
        try {
            long messageId = request.getMessageId();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void suspendUser(UserActionRequest request, StreamObserver<UserActionResponse> responseObserver) {
        try {
            long userId = request.getUserId();

            // Step 1: Get the user
            com.adia.user.UserResponse userResponse = userService.getUser(GetUserRequest.newBuilder().setId(userId).build());
            System.out.println("User response: " + userResponse);
            if (!userResponse.getSuccess()) {
                responseObserver.onNext(UserActionResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("User not found")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            // Step 2: Update user - set isSuspended = true
            com.adia.user.User user = userResponse.getUser();
            com.adia.user.UserResponse updateResponse = userService.updateUser(com.adia.user.UpdateUserRequest.newBuilder()
                    .setId(userId)
                    .setUsername(user.getUsername())
                    .setEmail(user.getEmail())
                    .setIsAdmin(user.getIsAdmin())
                    .setIsEmailVerified(user.getIsEmailVerified())
                    .setIsActivated(user.getIsActivated())
                    .setIsSuspended(true) // suspend the user here
                    .build());

            if (!updateResponse.getSuccess()) {
                responseObserver.onNext(UserActionResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Failed to suspend user: " + updateResponse.getMessage())
                        .build());
                responseObserver.onCompleted();
                return;
            }

            // Step 3: Return success
            responseObserver.onNext(UserActionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User suspended successfully")
                    .build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onNext(UserActionResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Exception: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }



}
