package com.chatbot.modules.tenant.core.controller;

import com.chatbot.modules.tenant.core.dto.CreateTenantRequest;
import com.chatbot.modules.tenant.core.dto.TenantResponse;
import com.chatbot.modules.tenant.core.mapper.TenantMapper;
import com.chatbot.modules.tenant.core.service.TenantService;

import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/me")
    public List<TenantResponse> getUserTenants() {
        return tenantService.getUserTenants().stream()
                .map(TenantMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    public TenantResponse create(@RequestBody CreateTenantRequest request) {
        // Không map ở đây, chuyền thẳng request vào Service
        return tenantService.createTenant(request); 
    }

    @PostMapping("/{id}/suspend")
    public void suspend(@PathVariable Long id) {
        tenantService.suspendTenant(id);
    }

    @PostMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        tenantService.activateTenant(id);
    }

    // 1. API lấy thông tin chi tiết của 1 tenant (kèm kiểm tra quyền sở hữu/tham gia)
    @GetMapping("/{id}")
    public TenantResponse getTenantById(@PathVariable Long id) {
        return tenantService.getTenantForCurrentUser(id);
    }

    // 2. API Switch Tenant (Phù hợp với frontend axios.post(`/tenants/${tenantId}/switch`))
    @PostMapping("/{id}/switch")
    public TenantResponse switchTenant(@PathVariable Long id) {
        return tenantService.switchTenant(id);
    }    
}
