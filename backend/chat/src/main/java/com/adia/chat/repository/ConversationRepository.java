package com.adia.chat.repository;

import com.adia.chat.entity.PrivateConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.adia.chat.entity.ConversationEntity;
import java.util.List;
 
@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, Long> {
} 