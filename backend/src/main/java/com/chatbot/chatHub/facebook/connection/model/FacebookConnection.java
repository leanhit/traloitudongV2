// src/main/java/com/chatbot/connections/models/FacebookConnection.java

package com.chatbot.chatHub.facebook.connection.model;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "facebook_connection")
public class FacebookConnection {
    
    @Id
    private UUID id;
    private String botId;
    private String botName;
    private String ownerId;
    private String pageId;
    private String fanpageUrl;
    private String pageAccessToken;
    private String fbUserId;
    private boolean isEnabled; // Trường mới
    private boolean isActive;
    
// 🎯 THÊM CÁC TRƯỜNG MỚI CHO CHATWOOT 🎯
    private Integer chatwootInboxId; 
    private String chatwootChannelKey; 
    // ------------------------------------
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
    /**
     * Phương thức kiểm tra xem kết nối Chatwoot đã được thiết lập thành công chưa.
     * Sử dụng để thay thế cho isChatwootConnected() trong logic Webhook.
     * @return true nếu có cả Inbox ID và Channel Key.
     */
    public boolean isChatwootConnected() {
        return this.chatwootInboxId != null && this.chatwootChannelKey != null;
    }
}