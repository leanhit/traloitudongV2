package com.chatbot.modules.tenant.core.dto;

import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.model.TenantVisibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TenantResponse {
    private Long id;
    private String name;
    private TenantStatus status;
    private TenantVisibility visibility;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
