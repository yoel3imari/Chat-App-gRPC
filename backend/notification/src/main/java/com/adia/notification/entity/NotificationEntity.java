package com.adia.notification.entity;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.*;

/**
 * Entity class for notifications
 */
@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_receiver_id", columnList = "receiver_id"),
                @Index(name = "idx_notifications_unread", columnList = "receiver_id, unread"),
                @Index(name = "idx_notifications_created_at", columnList = "created_at DESC")
        })
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiver_id", nullable = false)
    private String receiverId;

    @Column(name = "sender_id", nullable = false)
    private String senderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String title;

    @Column(length = 2048)
    private String link;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean unread = true;
}

