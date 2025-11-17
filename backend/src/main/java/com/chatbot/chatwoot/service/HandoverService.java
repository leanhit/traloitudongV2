package com.chatbot.chatwoot.service;

import com.chatbot.chatwoot.model.HandoverState;
import com.chatbot.chatwoot.repository.HandoverStateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class HandoverService {
    
    private final HandoverStateRepository repository;
    private static final long TIMEOUT_MINUTES = 2; // Ngưỡng thời gian Timeout là 2 phút
    private static final String CONVERSATION_KEY_FORMAT = "%s_%s"; // Format cho Key: senderId_pageId

    public HandoverService(HandoverStateRepository repository) {
        this.repository = repository;
    }

    /**
     * Lấy trạng thái Handover hiện tại của cuộc hội thoại và kiểm tra Timeout.
     * Đây là phương thức được gọi bởi FacebookWebhookService.
     */
    public HandoverState getState(String senderId, String pageId) {
        String key = String.format(CONVERSATION_KEY_FORMAT, senderId, pageId);
        
        // 1. Tìm hoặc tạo trạng thái mới
        HandoverState state = repository.findById(key).orElseGet(() -> {
            log.debug("Tạo trạng thái Handover mới cho user: {}", senderId);
            return new HandoverState(senderId, pageId);
        });

        // 2. Kiểm tra Timeout (Chỉ kiểm tra nếu Agent đang giữ quyền)
        if (state.isHandedOver()) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime timeoutTime = state.getLastAgentMessageAt().plusMinutes(TIMEOUT_MINUTES);
            
            if (now.isAfter(timeoutTime)) {
                // 3. Nếu Hết giờ: Chuyển quyền kiểm soát về Botpress
                log.info("⏰ Timeout {} phút: Đã chuyển quyền kiểm soát về Botpress cho user {}", TIMEOUT_MINUTES, senderId);
                state.setHandedOver(false); 
                state = repository.save(state); // Lưu trạng thái đã reset
            }
        }
        
        return state;
    }

    /**
     * Chuyển quyền kiểm soát cho Agent (Chatwoot) và reset timer.
     * Đây là phương thức được gọi bởi ChatwootWebhookService.
     */
    @Transactional
    public HandoverState handoverToAgent(String senderId, String pageId) {
        String key = String.format(CONVERSATION_KEY_FORMAT, senderId, pageId);
        
        // Tìm hoặc tạo trạng thái mới
        HandoverState state = repository.findById(key).orElse(new HandoverState(senderId, pageId));
        
        // Cập nhật trạng thái
        if (!state.isHandedOver()) {
             log.info("✅ Giao quyền thành công cho Agent: {}", senderId);
        } else {
             log.debug("🔄 Reset timer cho Agent: {}", senderId);
        }

        state.setHandedOver(true);
        state.setLastAgentMessageAt(LocalDateTime.now());
        
        return repository.save(state);
    }
    
    /**
     * Chuyển quyền kiểm soát về Botpress ngay lập tức (Nếu cần một API gọi từ Botpress để force reset).
     */
    @Transactional
    public HandoverState revertToBot(String senderId, String pageId) {
        String key = String.format(CONVERSATION_KEY_FORMAT, senderId, pageId);
        
        HandoverState state = repository.findById(key)
            .orElseThrow(() -> new RuntimeException("Conversation not found."));
            
        if (state.isHandedOver()) {
            state.setHandedOver(false);
            log.info("↩️ Quyền kiểm soát đã được chuyển về Botpress bởi lệnh API.");
            return repository.save(state);
        }
        return state;
    }
}