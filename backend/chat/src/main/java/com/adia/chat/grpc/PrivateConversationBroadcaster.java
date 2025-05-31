package com.adia.chat.grpc;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PrivateConversationBroadcaster {

    private final Map<Long, List<StreamObserver<PrivateConv>>> observers = new ConcurrentHashMap<>();

    public void register(long userId, StreamObserver<PrivateConv> observer) {
        if (observer instanceof ServerCallStreamObserver) {
            ServerCallStreamObserver<PrivateConv> serverObserver = (ServerCallStreamObserver<PrivateConv>) observer;
            serverObserver.setOnCancelHandler(() -> {
                unregister(userId, observer);
            });
        }
        observers.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(observer);
    }

    public void unregister(long userId, StreamObserver<PrivateConv> observer) {
        List<StreamObserver<PrivateConv>> list = observers.get(userId);
        if (list != null) {
            list.remove(observer);
            if (list.isEmpty()) {
                observers.remove(userId);
            }
        }
    }

    public void broadcast(long userId, PrivateConv conv) {
        List<StreamObserver<PrivateConv>> list = observers.get(userId);
        if (list != null) {
            for (StreamObserver<PrivateConv> observer : list) {
                try {
                    observer.onNext(conv);
                } catch (Exception e) {
                    unregister(userId, observer);
                }
            }
        }
    }

}
