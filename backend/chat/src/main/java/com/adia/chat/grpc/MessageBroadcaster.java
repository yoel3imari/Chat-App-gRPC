package com.adia.chat.grpc;

import com.adia.chat.grpc.Message;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.ServerCallStreamObserver;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageBroadcaster {
    private static final Logger logger = LoggerFactory.getLogger(MessageBroadcaster.class);
    
    // Map: conversationId -> list of observers
    private final Map<Long, List<StreamObserver<Message>>> observers = new ConcurrentHashMap<>();

    public void register(long conversationId, StreamObserver<Message> observer) {
        observers.computeIfAbsent(conversationId, k -> new CopyOnWriteArrayList<>()).add(observer);
        logger.debug("Registered observer for conversation {}. Total: {}",
                conversationId, observers.get(conversationId).size());
    }

    public void unregister(long conversationId, StreamObserver<Message> observer) {
        List<StreamObserver<Message>> list = observers.get(conversationId);
        if (list != null) {
            list.remove(observer);
            if (list.isEmpty()) {
                observers.remove(conversationId);
            }
        }
    }

    public void broadcast(long conversationId, Message message) {
        List<StreamObserver<Message>> list = observers.get(conversationId);
        if (list != null) {
            for (StreamObserver<Message> observer : list) {
                try {
                    observer.onNext(message);
                } catch (Exception e) {
                    logger.warn("Failed to send message to client in conversation {}: {}", conversationId, e.getMessage());
                    unregister(conversationId, observer);
                }
            }
        }
    }
} 