package com.adia.notification.grpc;

import com.adia.chat.grpc.ChatServiceGrpc;
import com.adia.notification.*;
import com.adia.notification.entity.NotificationEntity;
import com.adia.notification.entity.NotificationMapper;
import com.adia.notification.repository.NotificationRepository;
import com.adia.user.UserServiceGrpc;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import java.time.Instant;
import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class NotificationServiceImpl extends NotificationServiceGrpc.NotificationServiceImplBase {
    private final NotificationBroadcaster broadcaster;
    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    @GrpcClient("chat-service")
    private ChatServiceGrpc.ChatServiceBlockingStub chatService;

    @Override
    public void getAllNotifications(UserRequest request, StreamObserver<NotificationList> responseObserver) {
        try {
            List<NotificationEntity> notifications =
                    repository.findByReceiverIdOrderByCreatedAtDesc(request.getUserId());

            int unreadCount = (int) notifications.stream().filter(NotificationEntity::isUnread).count();

            NotificationList response = NotificationList.newBuilder()
                    .addAllNotifications(notifications.stream().map(mapper::toGrpc).toList())
                    .setUnreadCount(unreadCount)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to fetch notifications" + e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    /**
     * Broadcasr a notif to all users
     * @param request
     * @param responseObserver
     */
    @Override
    public void createNotification(NewNotificationRequest request, StreamObserver<Notification> responseObserver) {
        try {
            NotificationEntity entity = NotificationEntity.builder()
                    .receiverId(request.getUserId())
                    .content(request.getMessage())
                    .title("Notification")
                    .unread(true)
                    .createdAt(Instant.now())
                    .build();

            NotificationEntity saved = repository.save(entity);
            broadcaster.broadcastToAll(mapper.toGrpc(saved));
            responseObserver.onNext(mapper.toGrpc(saved));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to create notification").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void notifyGroupInvite(GroupInviteRequest request, StreamObserver<Notification> responseObserver) {
        try {
            NotificationEntity entity = NotificationEntity.builder()
                    .receiverId(request.getInviteeId())
                    .senderId(request.getInviterId())
                    .content("You were invited to join group " + request.getGroupId())
                    .title("Group Invite")
                    .link("/groups/" + request.getGroupId())
                    .unread(true)
                    .createdAt(Instant.now())
                    .build();

            NotificationEntity saved = repository.save(entity);
            broadcaster.broadcastToUser(request.getInviteeId(), mapper.toGrpc(saved));
            responseObserver.onNext(mapper.toGrpc(saved));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to send group invite").withCause(e).asRuntimeException());
        }
    }

    /**
     * Send a warning to a user
     * */
    @Override
    public void sendNotification(SendNotifReq request, StreamObserver<Empty> responseObserver) {
        try {
            NotificationEntity entity = new NotificationEntity();
            entity.setReceiverId(request.getReceiverId());
            entity.setContent(request.getContent());
            entity.setTitle(request.getTitle());
            entity.setUnread(true);
            entity.setCreatedAt(Instant.now());
            entity.setSenderId(request.getSenderId());
            entity.setLink("#");

            NotificationEntity saved = repository.save(entity);
            broadcaster.broadcastToUser(request.getReceiverId(), mapper.toGrpc(saved));
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to send notification").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void streamNotifications(UserRequest request, StreamObserver<Notification> responseObserver) {
        String userId = request.getUserId();

        broadcaster.register(userId, responseObserver);

        // Note: Unregister when client disconnects
        ((ServerCallStreamObserver<Notification>) responseObserver).setOnCancelHandler(() -> {
            broadcaster.unregister(userId, responseObserver);
        });
    }
}