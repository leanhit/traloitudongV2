package com.chatbot.configs;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class ChatwootApiConfig {

    // URL gốc của Chatwoot Server của bạn (ví dụ: https://chatwoot.yourdomain.com)
    @Value("${chatwoot.api.url}")
    private String apiUrl;

    // Token truy cập quản trị viên (Management API Token)
    @Value("${chatwoot.api.access-token}")
    private String accessToken;

    // ID tài khoản (Account ID) trong Chatwoot mà các Inbox sẽ thuộc về
    @Value("${chatwoot.account.id}")
    private String accountId;
}