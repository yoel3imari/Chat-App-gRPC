package com.adia.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"conversation"}) // userId is a Long, no need to exclude typically
@EqualsAndHashCode(of = "id")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob // For potentially long text, maps to TEXT SQL type
    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Changed from UserEntity user (Sender of the message)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ConversationEntity conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status = MessageStatus.sent;

    @Column(name = "is_edited", nullable = false)
    private boolean isEdited = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}