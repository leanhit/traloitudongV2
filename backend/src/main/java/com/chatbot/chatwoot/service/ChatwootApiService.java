package com.chatbot.chatwoot.service;

import com.chatbot.configs.ChatwootApiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
public class ChatwootApiService {

    private final RestTemplate restTemplate;
    private final ChatwootApiConfig config;

    public ChatwootApiService(RestTemplate restTemplate, ChatwootApiConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        // Chatwoot yêu cầu header API_ACCESS_TOKEN
        headers.set("api_access_token", config.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * Tạo một API Inbox mới trong Chatwoot cho một Fanpage.
     * @param pageName Tên Fanpage
     * @return Map chứa inbox_id và inbox_api_key
     */
    public Map<String, Object> createApiInbox(String pageName) {
        String url = String.format("%s/api/v1/accounts/%s/inboxes", 
                config.getApiUrl(), config.getAccountId());
        
        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("name", pageName + " (FB Chatbot)");
        body.put("channel_type", "api");              // phải lowercase
        body.put("greeting_enabled", false);
        body.put("enable_auto_assignment", true);

        // CHỈ THÊM selected_agents nếu ID hợp lệ
        // body.put("selected_agents", List.of(7));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            Map<String, Object> inboxData =
                    (Map<String, Object>) response.getBody().get("data");

            Map<String, Object> result = new HashMap<>();
            result.put("inbox_id", inboxData.get("id"));
            result.put("inbox_api_key", inboxData.get("channel_key"));

            return result;

        } catch (Exception e) {
            log.error("❌ Lỗi tạo inbox: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo Chatwoot Inbox", e);
        }
    }

    
    // ... Cần bổ sung các phương thức khác: deleteInbox, createMessage (ghi log), etc.
    
    /**
     * Xóa một Inbox trong Chatwoot.
     */
    public void deleteInbox(Integer inboxId) {
        if (inboxId == null) return;
        String url = String.format("%s/api/v1/accounts/%s/inboxes/%d", config.getApiUrl(), config.getAccountId(), inboxId);
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.info("Đã xóa Chatwoot Inbox ID: {}", inboxId);
        } catch (Exception e) {
            log.warn("Cảnh báo: Không thể xóa Chatwoot Inbox ID {}. Lỗi: {}", inboxId, e.getMessage());
        }
    }
    /**
     * Ghi log tin nhắn đến (Khách hàng -> Middleware -> Chatwoot).
     * Endpoint: POST /api/v1/inboxes/{inboxId}/incoming_messages
     * @param inboxId ID Inbox Chatwoot
     * @param senderId Facebook User ID
     * @param content Nội dung tin nhắn
     */
    public void logInboundMessage(Integer inboxId, String senderId, String content) {
        String url = String.format("%s/api/v1/inboxes/%d/incoming_messages", config.getApiUrl(), inboxId);
        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("sender_id", senderId);
        body.put("content", content);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // Sử dụng exchange để tránh lỗi khi response body là empty/null
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.debug("📝 [Chatwoot] Đã ghi log tin nhắn INBOUND cho Inbox ID {}", inboxId);
        } catch (Exception e) {
            log.error("❌ [Chatwoot] Lỗi ghi log tin nhắn INBOUND cho Inbox ID {}: {}", inboxId, e.getMessage());
            // Không throw exception để không làm gián đoạn luồng xử lý tin nhắn chính
        }
    }

    /**
     * Ghi log tin nhắn đi (Botpress/Agent -> Middleware -> Chatwoot).
     * Endpoint: POST /api/v1/inboxes/{inboxId}/outgoing_messages
     * @param inboxId ID Inbox Chatwoot
     * @param senderId Facebook User ID
     * @param content Nội dung tin nhắn
     */
    public void logOutboundBotMessage(Integer inboxId, String senderId, String content) {
        String url = String.format("%s/api/v1/inboxes/%d/outgoing_messages", config.getApiUrl(), inboxId);
        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("sender_id", senderId);
        body.put("content", content);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.debug("📝 [Chatwoot] Đã ghi log tin nhắn OUTBOUND cho Inbox ID {}", inboxId);
        } catch (Exception e) {
            log.error("❌ [Chatwoot] Lỗi ghi log tin nhắn OUTBOUND cho Inbox ID {}: {}", inboxId, e.getMessage());
        }
    }
}