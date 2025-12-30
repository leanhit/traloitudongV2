package com.chatbot.modules.tenant.core.dto;

import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.model.TenantVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSearchResponse {
    private Long id;
    private String name;
    private String description;
    private TenantStatus status;
    private TenantVisibility visibility;
    private LocalDateTime createdAt;
    private TenantMembershipStatus membershipStatus;
}
