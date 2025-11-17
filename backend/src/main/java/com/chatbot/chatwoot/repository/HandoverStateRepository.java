package com.chatbot.chatwoot.repository;

import com.chatbot.chatwoot.model.HandoverState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HandoverStateRepository extends JpaRepository<HandoverState, String> {
    
    // Phương thức tìm kiếm bổ sung nếu cần, nhưng findById(conversationKey) là đủ
    Optional<HandoverState> findByFacebookSenderIdAndFacebookPageId(String senderId, String pageId);
}