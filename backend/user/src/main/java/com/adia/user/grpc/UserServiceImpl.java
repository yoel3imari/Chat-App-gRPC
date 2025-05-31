package com.adia.user.grpc;

import com.adia.auth.AuthResponse;
import com.adia.notification.NewNotificationRequest;
import com.adia.notification.Notification;
import com.adia.notification.NotificationServiceGrpc;
import com.adia.user.*;
import com.adia.user.entity.UserEntity;
import com.adia.user.repository.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@GrpcService
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {

            System.out.println(request.toString());

            if (userRepository.existsByEmail(request.getEmail())) {
                responseObserver.onError(Status.ALREADY_EXISTS
                        .withDescription("Email already in use")
                        .asRuntimeException());
                return;
            }

            if (userRepository.existsByUsername(request.getUsername())) {
                responseObserver.onError(Status.ALREADY_EXISTS
                        .withDescription("Username already in use")
                        .asRuntimeException());
                return;
            }

            // Map request to entity
            UserEntity userEntity = UserEntity.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword())) // hash this in real use!
                    .isAdmin(request.getIsAdmin())
                    .isEmailVerified(request.getIsEmailVerified())
                    .isActivated(request.getIsActivated())
                    .isSuspended(request.getIsSuspended())
                    .build();

            // Save to DB
            userEntity = userRepository.save(userEntity);

            // Build response
            UserResponse response = UserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User created successfully")
                    .setUser(toProtoUser(userEntity))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {

            System.out.println(e.getMessage());

            responseObserver.onNext(UserResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        try {
            int page = request.getPage(); // 1-based index from frontend
            int limit = request.getLimit();

            Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit);
            Page<UserEntity> pageResult = userRepository.findAll(pageable);

            ListUsersResponse.Builder responseBuilder = ListUsersResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Users retrieved successfully")
                .setTotal((int) pageResult.getTotalElements());

            for (UserEntity entity : pageResult.getContent()) {
                User user = this.toProtoUser(entity).build();
                responseBuilder.addUsers(user);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(ListUsersResponse.newBuilder()
                    .setMessage("Error: " + e.getMessage())
                    .setSuccess(false)
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUserStats(Empty request, StreamObserver<UserStatsResponse> responseObserver) {
        try {
            int total = Math.toIntExact( userRepository.count());
            int suspended = Math.toIntExact( userRepository.countByIsSuspendedTrue());
            int unverified = Math.toIntExact( userRepository.countByIsEmailVerifiedFalse());
            int active = Math.toIntExact( userRepository.countByIsActivatedTrue());

            UserStatsResponse response = UserStatsResponse.newBuilder()
                    .setTotal(total)
                    .setActive(active)
                    .setSuspended(suspended)
                    .setUnverified(unverified)
                    .setSuccess(true)
                    .setMessage("Stats retrieved successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(UserStatsResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            Optional<UserEntity> userEntity = userRepository.getUserEntityById(request.getId());

            if (userEntity.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("User not found")
                        .asRuntimeException());
                return;
            }

            UserResponse response = UserResponse.newBuilder()
                    .setMessage("User retrived")
                    .setSuccess(true)
                    .setUser(toProtoUser(userEntity.get()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onNext(UserResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void suspendUser(SuspendUserRequest request, StreamObserver<SuspendUserResponse> responseObserver) {
        try {
            Optional<UserEntity> userOpt = userRepository.findById(Long.parseLong(request.getUserId()));
            if (userOpt.isEmpty()) {
                responseObserver.onNext(SuspendUserResponse.newBuilder()
                        .setSuccess(false).setMessage("User not found").build());
                responseObserver.onCompleted();
                return;
            }

            if (userOpt.get().getIsAdmin()) {
                responseObserver.onNext(SuspendUserResponse.newBuilder()
                        .setSuccess(false).setMessage("Cannot suspend an admin").build());
                responseObserver.onCompleted();
            }

            UserEntity user = userOpt.get();
            // actually toggle Suspend
            user.setIsSuspended(!user.getIsSuspended());
            userRepository.save(user);

            responseObserver.onNext(SuspendUserResponse.newBuilder()
                    .setSuccess(true).setMessage("User suspended").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(SuspendUserResponse.newBuilder()
                    .setSuccess(false).setMessage("Error: " + e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        try {
            Optional<UserEntity> userOpt = userRepository.findById(Long.parseLong(request.getUserId()));
            if (userOpt.isEmpty()) {
                responseObserver.onNext(DeleteUserResponse.newBuilder()
                        .setSuccess(false).setMessage("User not found").build());
                responseObserver.onCompleted();
                return;
            }

            UserEntity user = userOpt.get();
            if (user.getIsAdmin()) {
                responseObserver.onNext(DeleteUserResponse.newBuilder()
                        .setSuccess(false).setMessage("Cannot delete an admin").build());
                responseObserver.onCompleted();
                return;
            }

            userRepository.deleteById(user.getId());
            responseObserver.onNext(DeleteUserResponse.newBuilder()
                    .setSuccess(true).setMessage("User deleted").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(DeleteUserResponse.newBuilder()
                    .setSuccess(false).setMessage("Error: " + e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            Optional<UserEntity> userEntity = userRepository.getUserEntityByEmail(request.getEmail());

            if (userEntity.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("User not found")
                        .asRuntimeException());
                return;
            }

            UserResponse response = UserResponse.newBuilder()
                    .setMessage("User retrived")
                    .setSuccess(true)
                    .setUser(toProtoUser(userEntity.get()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onNext(UserResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void searchUsers(SearchReq request, StreamObserver<SearchRes> responseObserver) {
        try {
            List<UserEntity> userEntities = userRepository.findDistinctByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(request.getSearchTerm(), request.getSearchTerm());

            // Convert UserEntity objects to protobuf User objects
            List<User> users = userEntities.stream()
                    .map(this::toProtoUser)
                    .map(User.Builder::build)
                    .collect(Collectors.toList());

            // Build the response with the list of users
            SearchRes response = SearchRes.newBuilder()
                    .addAllUsers(users)
                    .build();

            // Send the response
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error searching users: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void verifyPassword(VPasswordReq request, StreamObserver<VPasswordRes> responseObserver) {
        try {

            String password = request.getPassword();
            String email = request.getEmail();

            Optional<UserEntity> userEntity = userRepository.getUserEntityByEmail(email);

            if (userEntity.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("User not found")
                        .asRuntimeException());
                return;
            }

            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (!passwordEncoder.matches(password, userEntity.get().getPassword())) {
                responseObserver.onNext(VPasswordRes.newBuilder()
                        .setSuccess(false)
                        .setMessage("Wrong password")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            responseObserver.onNext(VPasswordRes.newBuilder()
                    .setSuccess(true)
                    .setMessage("Correct password")
                    .build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onNext(VPasswordRes.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    private User.Builder toProtoUser(UserEntity user) {
        return User.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setIsAdmin(user.getIsAdmin())
                .setIsEmailVerified(user.getIsEmailVerified())
                .setIsActivated(user.getIsActivated())
                .setIsSuspended(user.getIsSuspended())
                .setCreatedAt(user.getCreatedAt().toString())
                .setUpdatedAt(user.getUpdatedAt().toString());
    }

    private void respond(boolean success, String msg, StreamObserver<UserActionResponse> obs) {
        obs.onNext(UserActionResponse.newBuilder()
                .setSuccess(success)
                .setMessage(msg)
                .build());
        obs.onCompleted();
    }
}


