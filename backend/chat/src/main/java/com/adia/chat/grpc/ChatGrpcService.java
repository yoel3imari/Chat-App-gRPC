package com.adia.chat.grpc;

import com.adia.chat.entity.ConversationEntity;
import com.adia.chat.entity.ConversationType;
import com.adia.chat.entity.MessageEntity;
import com.adia.chat.entity.MessageStatus;
import com.adia.chat.entity.PrivateConversationEntity;
import com.adia.chat.repository.*;
import com.adia.user.GetUserRequest;
import com.adia.user.User;
import com.adia.user.UserResponse;
import com.adia.user.UserServiceGrpc;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@GrpcService
public class    ChatGrpcService extends ChatServiceGrpc.ChatServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ChatGrpcService.class);
    private static final MessageBroadcaster messageBroadcaster = new MessageBroadcaster();
    private static final PrivateConversationBroadcaster privateConvBroadcaster = new PrivateConversationBroadcaster();


    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userService;

    private final ConversationRepository conversationRepo;
    private final PrivateConversationRepository privateConvRepo;
    private final MessageRepository messageRepo;

    public ChatGrpcService(
        ConversationRepository conversationRepo,
        PrivateConversationRepository privateConvRepo,
        MessageRepository messageRepository
    ) {
        this.conversationRepo = conversationRepo;
        this.privateConvRepo = privateConvRepo;
        this.messageRepo = messageRepository;
    }
    
    @Override
    public void getPrivateConversations(PrivateConvsReq request, StreamObserver<GetPrivConvsRes> responseObserver) {
        try {
            Long userId = request.getUserId();
            logger.info("Fetching private conversations for user ID: {}", userId);
            
            // Validate userId
            if (userId == null || userId <= 0) {
                throw new IllegalArgumentException("Invalid user ID");
            }

            // Fetch private conversations for the user
            List<PrivateConversationEntity> privConvs = privateConvRepo.findByConversationOwnerIdOrReceiverId(userId);
            List<PrivateConv> result = new ArrayList<>();

            // Map each entity to a gRPC message
            for (PrivateConversationEntity entity : privConvs) {
                try {
                    PrivateConv grpcConv = mapToGrpcPrivateConv(entity, userId);
                    result.add(grpcConv);
                } catch (Exception e) {
                    logger.warn("Error mapping conversation {}: {}", entity.getId(), e.getMessage());
                    // Continue with next conversation instead of failing the entire request
                }
            }

            // Build and send the response
            GetPrivConvsRes response = GetPrivConvsRes.newBuilder()
                    .setMessage("Fetched " + privConvs.size() + " () conversations successfully")
                    .setSuccess(true)
                    .addAllPrivateConvList(result)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            logger.info("Successfully returned {} private conversations for user ID: {}", result.size(), userId);

        } catch (Exception e) {
            logger.error("Error in getPrivateConversations: {}", e.getMessage(), e);
            GetPrivConvsRes errorResponse = GetPrivConvsRes.newBuilder()
                    .setMessage("Failed to fetch private conversations: " + e.getMessage())
                    .setSuccess(false)
                    .build();
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void createPrivateConversation(CreatePrivConvReq request, StreamObserver<PrivateConv> responseObserver) {
        try {
            Long currUserId = request.getCurrUserId();
            Long otherUserId = request.getOtherUserId();
            
            // Check if a conversation already exists between these users
            Optional<PrivateConversationEntity> existingConversation = 
                privateConvRepo.findByConversationOwnerIdAndReceiverId(currUserId, otherUserId);

            PrivateConversationEntity privateConversation;
            
            if (existingConversation.isPresent()) {
                // Use existing conversation
                privateConversation = existingConversation.get();
            } else {
                // Create a new conversation
                ConversationEntity conversation = new ConversationEntity();
                conversation.setOwnerId(currUserId);
                conversation.setType(ConversationType.PRIVATE);
                conversation = conversationRepo.save(conversation);
                
                // Create private conversation details
                privateConversation = new PrivateConversationEntity();
                privateConversation.setConversation(conversation);
                privateConversation.setReceiverId(otherUserId);
                privateConversation = privateConvRepo.save(privateConversation);
                
                // Set the bidirectional relationship
                conversation.setPrivateConversationDetails(privateConversation);
                conversationRepo.save(conversation);
            }

            // get Other User
            UserResponse userResOther = userService.getUser(GetUserRequest.newBuilder().setId(otherUserId).build());
            // get Current User
            UserResponse userResCurr = userService.getUser(GetUserRequest.newBuilder().setId(currUserId).build());
            
            // Build the response
            PrivateConv response = PrivateConv.newBuilder()
                    .setId(privateConversation.getId())
                    .setUser2(userResOther.getUser())
                    .setUser1(userResCurr.getUser())
                    .setUnreadCount(0)
                    .setLastMessage("")
                    .setLastUpdate(toProtoTimestamp(privateConversation.getUpdatedAt()))
                    .build();

            // broadcast the new conversation to the other user
            privateConvBroadcaster.broadcast(response.getUser2().getId(), response);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error in createPrivateConversation: {}", e.getMessage(), e);
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Failed to create private conversation: " + e.getMessage())
                    .asRuntimeException()
            );
        }
    }

    @Override
    public void getPrivateConversation(GetPrivConvReq request, StreamObserver<GetPrivConvRes> responseObserver) {
        try {
            Optional<PrivateConversationEntity> conversationOpt =
                privateConvRepo.findByConversationOwnerIdAndReceiverId(
                        request.getCurrUserId(),
                        request.getOtherUserId());

            if (conversationOpt.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Conversation not found between the specified users.")
                        .asRuntimeException());
                return;
            }

            PrivateConversationEntity conversation = conversationOpt.get();

            List<MessageEntity> messages = messageRepo.findByConversationId(conversation.getId());

            GetPrivConvRes response = GetPrivConvRes.newBuilder()
                    .setId(conversation.getId())
                    .addAllMessages(messages.stream()
                            .map(this::toGrpcMessage)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error")
                    .augmentDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getConvMessage(ConvMsgReq request, StreamObserver<ConvMsgRes> responseObserver) {
        try {
            long convId = request.getConvId();
            logger.info("GetConvMsg: conversation with convId: {}", convId);

            Optional<PrivateConversationEntity> conversationOpt = privateConvRepo.findById(convId);
            if (conversationOpt.isEmpty()) {
                logger.warn("Conversation not found for convId: {}", convId);
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Conversation not found with ID: " + convId)
                        .asRuntimeException());
                return;
            }

            Set<MessageEntity> messages = conversationOpt.get()
                    .getConversation()
                    .getMessages();

            List<Message> grpcMessages = messages.stream()
                    .map(this::toGrpcMessage)
                    .toList();

            ConvMsgRes response = ConvMsgRes.newBuilder()
                    .setMessage("Fetched " + grpcMessages.size() + " messages successfully")
                    .setSuccess(true)
                    .addAllMessageList(grpcMessages)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Error fetching conversation messages for convId: {}", request.getConvId(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error while fetching conversation messages")
                    .augmentDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void sendMessage(MessageReq request, StreamObserver<Empty> responseObserver) {
        try {
            long userId = request.getUserId();
            long convId = request.getConvId();
            String text = request.getText();

            logger.info("Looking for conversation with convId: {}, user {}", convId, userId);

            // Check if the user is allowed to send a message in this conversation
            Optional<PrivateConversationEntity> conversationOpt = privateConvRepo.findById(convId);
            logger.info("Found conversation: {}", conversationOpt.isPresent());

            if (conversationOpt.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Conversation not found")
                        .asRuntimeException());
                return;
            }
            PrivateConversationEntity privateConv = conversationOpt.get();

            // create new MessageEntity
            MessageEntity message = new MessageEntity();
            message.setUserId(userId);
            message.setConversation(privateConv.getConversation());
            message.setText(text);
            message.setEdited(false);
            message.setStatus(MessageStatus.sent);
            message = messageRepo.save(message);

            // broadcast new message to all user registered to this conversation
            messageBroadcaster.broadcast(convId, toGrpcMessage(message));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed to send message", e.getMessage()); // ← Add this
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error")
                    .augmentDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void streamPrivateConversations(UserId request, StreamObserver<PrivateConv> responseObserver) {
        try {
            long userId = request.getUserId();
            logger.info("Streaming private conversations for user ID: {}", userId);

            privateConvBroadcaster.register(userId, responseObserver);
            ((ServerCallStreamObserver<PrivateConv>) responseObserver).setOnCancelHandler(() -> {
                privateConvBroadcaster.unregister(userId, responseObserver);
            });
        } catch (Exception e) {
            logger.error("Error in streamPrivateConversations: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error")
                    .augmentDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void streamMessages(ConversationId request, StreamObserver<Message> responseObserver) {
        try {
            long convId = request.getConvId();

            logger.info("Streaming messages conversation ID: {}", convId);

            messageBroadcaster.register(convId, responseObserver);

            // Handle cancellation in the service layer
            if (responseObserver instanceof ServerCallStreamObserver) {
                ((ServerCallStreamObserver<Message>) responseObserver).setOnCancelHandler(() -> {
                    logger.debug("Client cancelled stream for conversation {}", convId);
                    messageBroadcaster.unregister(convId, responseObserver);
                });
            }
        } catch (Exception e) {
            logger.error("Error in streamPrivateConversations: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error")
                    .augmentDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    private PrivateConv mapToGrpcPrivateConv(PrivateConversationEntity entity, Long currentUserId) {
        ConversationEntity conversation = entity.getConversation();

        // Determine who is the 'other user' in this private conversation
        Long otherUserId;
        if (conversation.getOwnerId().equals(currentUserId)) {
            otherUserId = entity.getReceiverId();
        } else {
            otherUserId = conversation.getOwnerId();
        }

        // Fetch other user details from user service
        User otherUser = null;
        try {
            UserResponse userRes = userService.getUser(GetUserRequest.newBuilder().setId(otherUserId).build());
            if (userRes.getSuccess() && userRes.hasUser()) {
                otherUser = userRes.getUser();
            } else {
                logger.warn("Other user with ID {} not found for conversation {}", otherUserId, entity.getId());
            }
        } catch (Exception e) {
            logger.error("Error fetching user details for ID {}: {}", otherUserId, e.getMessage());
        }

        // Get the last message
        String lastMessageText = "";
        Set<MessageEntity> messages = conversation.getMessages();
        if (messages != null && !messages.isEmpty()) {
            Optional<MessageEntity> lastMsgOpt = messages.stream()
                    .max(Comparator.comparing(MessageEntity::getCreatedAt));
            lastMessageText = lastMsgOpt.map(MessageEntity::getText).orElse("");
        }

        // Calculate unread count
        long unreadCount = 0;
        if (messages != null) {
            unreadCount = messages.stream()
                    .filter(msg -> !msg.getUserId().equals(currentUserId) && msg.getStatus() != MessageStatus.read)
                    .count();
        }

        // Convert LocalDateTime to Protobuf Timestamp
        Timestamp lastUpdateTimestamp = toProtoTimestamp(conversation.getUpdatedAt());

        // Build the PrivateConv message
        PrivateConv.Builder builder = PrivateConv.newBuilder()
                .setId(entity.getId())
                .setLastMessage(lastMessageText)
                .setUnreadCount((int) unreadCount)
                .setLastUpdate(lastUpdateTimestamp);

        // Set other user if available
        if (otherUser != null) {
            builder.setUser2(otherUser);
        }

        User currUser = userService.getUser(GetUserRequest.newBuilder().setId(currentUserId).build()).getUser();
        builder.setUser1(currUser);

        return builder.build();
    }

    private Message toGrpcMessage(MessageEntity msg) {
        return Message.newBuilder()
                .setId(msg.getId())
                .setUserId(msg.getUserId())
                .setConversationId(msg.getConversation().getId())
                .setText(msg.getText())
                .setEdited(msg.isEdited())
                .setStatusValue(msg.getStatus().ordinal())
                .setCreatedAt(msg.getCreatedAt().toString())
                .setUpdatedAt(msg.getUpdatedAt().toString())
                .build();
    }

    public static Timestamp toProtoTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}