package com.adia.notification.grpc;

import com.adia.notification.Notification;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class NotificationBroadcaster {

    private final ConcurrentMap<String, List<StreamObserver<Notification>>> observers = new ConcurrentHashMap<>();

    public void register(String userId, StreamObserver<Notification> observer) {
        observers.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(observer);
    }

    public void unregister(String userId, StreamObserver<Notification> observer) {
        List<StreamObserver<Notification>> list = observers.get(userId);
        if (list != null) {
            list.remove(observer);
            if (list.isEmpty()) {
                observers.remove(userId);
            }
        }
    }

    public void broadcastToUser(String userId, Notification notification) {
        List<StreamObserver<Notification>> list = observers.get(userId);
        if (list != null) {
            for (StreamObserver<Notification> observer : list) {
                try {
                    observer.onNext(notification);
                } catch (Exception e) {
                    unregister(userId, observer);
                }
            }
        }
    }

    // optional: broadcast to all users
    public void broadcastToAll(Notification notification) {
        observers.forEach((userId, list) -> broadcastToUser(userId, notification));
    }
}

