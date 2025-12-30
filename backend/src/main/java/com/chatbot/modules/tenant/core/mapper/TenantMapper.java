package com.chatbot.modules.tenant.core.mapper;

import com.chatbot.modules.tenant.core.dto.CreateTenantRequest;
import com.chatbot.modules.tenant.core.dto.TenantResponse;
import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.core.model.TenantStatus;

import java.time.LocalDateTime;

public class TenantMapper {

    private TenantMapper() {}

    // Request → Entity
    public static Tenant toEntity(CreateTenantRequest request) {
        return Tenant.builder()
                .name(request.getName())
                .status(TenantStatus.ACTIVE)
                .visibility(request.getVisibility())
                .expiresAt(LocalDateTime.now().plusDays(30)) // trial
                .build();
    }

    // Entity → Response
    public static TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .status(tenant.getStatus())
                .visibility(tenant.getVisibility())
                .expiresAt(tenant.getExpiresAt())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
