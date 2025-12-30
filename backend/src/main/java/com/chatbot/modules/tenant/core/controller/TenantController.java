package com.chatbot.modules.tenant.core.controller;

import com.chatbot.modules.tenant.core.dto.CreateTenantRequest;
import com.chatbot.modules.tenant.core.dto.TenantResponse;
import com.chatbot.modules.tenant.core.dto.TenantSearchRequest;
import com.chatbot.modules.tenant.core.dto.TenantSearchResponse;
import com.chatbot.modules.tenant.core.mapper.TenantMapper;
import com.chatbot.modules.tenant.core.service.TenantService;

import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * Lấy danh sách tenant của user hiện tại.
     */
    @GetMapping("/me")
    public List<TenantResponse> getUserTenants() {
        return tenantService.getUserTenants()
                .stream()
                .map(TenantMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tạo tenant mới.
     */
    @PostMapping
    public TenantResponse create(@RequestBody CreateTenantRequest request) {
        return tenantService.createTenant(request);
    }

    /**
     * Suspend tenant (OWNER).
     */
    @PostMapping("/{id}/suspend")
    public void suspend(@PathVariable Long id) {
        tenantService.suspendTenant(id);
    }

    /**
     * Activate tenant.
     */
    @PostMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        tenantService.activateTenant(id);
    }

    /**
     * Lấy chi tiết tenant (user phải là member).
     */
    @GetMapping("/{id}")
    public TenantResponse getTenantById(@PathVariable Long id) {
        return tenantService.getTenantForCurrentUser(id);
    }

    /**
     * Switch tenant hiện tại.
     */
    @PostMapping("/{id}/switch")
    public TenantResponse switchTenant(@PathVariable Long id) {
        return tenantService.switchTenant(id);
    }

    /**
     * Search tenant.
     */
    @GetMapping("/search")
    public Page<TenantSearchResponse> searchTenants(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        String currentUserEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();

        TenantSearchRequest request = new TenantSearchRequest();
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        return tenantService.searchTenants(request, currentUserEmail);
    }
}
