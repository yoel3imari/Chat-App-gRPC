package com.adia.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adia.chat.entity.GroupConversationEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupConversationRepository extends JpaRepository<GroupConversationEntity, Long> {
    // Get all GROUP conversations where user is either owner or member
    @Query(value = """
        SELECT DISTINCT c.* FROM conversations c
        JOIN group_conversations gc ON gc.conversation_id = c.id
        JOIN group_members gm ON gm.group_id = gc.id
        WHERE c.owner_id = :userId
           OR gm.user_id = :userId
        """, nativeQuery = true)
    List<GroupConversationEntity> findGroupConversationsByUser(@Param("userId") Long userId);
} 