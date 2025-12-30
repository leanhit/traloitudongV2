package com.chatbot.modules.tenant.membership.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TenantPendingResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime requestedAt;
}
