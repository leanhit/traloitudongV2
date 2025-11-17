package com.chatbot.chatwoot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ trạng thái giao quyền kiểm soát (Botpress vs Agent) cho từng cuộc hội thoại.
 * Sử dụng composite key hoặc kết hợp senderId và pageId làm ID.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "facebook_handover_state")
public class HandoverState {

    // Sử dụng kết hợp Sender ID và Page ID để tạo ID duy nhất cho cuộc hội thoại
    @Id
    private String conversationKey; 

    private String facebookSenderId;   // ID người dùng Facebook
    private String facebookPageId;     // ID Fanpage

    // Trạng thái: TRUE nếu quyền kiểm soát đang thuộc về Agent (Chatwoot)
    private boolean isHandedOver;    
    
    // Thời điểm cuối cùng Agent gửi tin nhắn
    private LocalDateTime lastAgentMessageAt; 

    // Constructor để tạo Key
    public HandoverState(String facebookSenderId, String facebookPageId) {
        this.facebookSenderId = facebookSenderId;
        this.facebookPageId = facebookPageId;
        // Tạo Key: senderId_pageId
        this.conversationKey = facebookSenderId + "_" + facebookPageId;
        this.isHandedOver = false;
        this.lastAgentMessageAt = LocalDateTime.now();
    }
}