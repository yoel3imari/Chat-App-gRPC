package com.adia.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "private_conversations")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"conversation"}) // receiverId is a Long
@EqualsAndHashCode(of = "id")
public class PrivateConversationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "conversation_id", nullable = false, unique = true)
    private ConversationEntity conversation;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId; // Changed from UserEntity receiver

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}