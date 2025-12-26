package com.chatbot.modules.tenant.core.dto;

import com.chatbot.modules.tenant.core.model.TenantStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TenantResponse {
    private Long id;
    private String name;
    private TenantStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
