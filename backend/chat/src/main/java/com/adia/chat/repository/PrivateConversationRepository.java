package com.adia.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adia.chat.entity.PrivateConversationEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivateConversationRepository extends JpaRepository<PrivateConversationEntity, Long> {
    // Get all PRIVATE conversations where user is either owner or receiver
    @Query(value = """
        SELECT DISTINCT pc FROM PrivateConversationEntity pc
        WHERE pc.conversation.ownerId = :userId OR pc.receiverId = :userId
        ORDER BY pc.id DESC
    """)
    List<PrivateConversationEntity> findByConversationOwnerIdOrReceiverId(@Param("userId") Long userId);

    @Query("""
    SELECT pc FROM PrivateConversationEntity pc
    WHERE (pc.conversation.ownerId = :user1Id AND pc.receiverId = :user2Id)
       OR (pc.conversation.ownerId = :user2Id AND pc.receiverId = :user1Id)
    ORDER BY pc.id ASC
    """)
    List<PrivateConversationEntity> findByUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    @Query("""
           SELECT pc FROM PrivateConversationEntity pc
           WHERE (pc.conversation.ownerId = :ownerId AND pc.receiverId = :receiverId)
               OR (pc.conversation.ownerId = :receiverId AND pc.receiverId = :ownerId)
    """)
    Optional<PrivateConversationEntity> findByConversationOwnerIdAndReceiverId(
            @Param("ownerId") Long ownerId,
            @Param("receiverId") Long receiverId);

} 