package com.chatbot.modules.tenant.core.service;

import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.tenant.core.dto.CreateTenantRequest;
import com.chatbot.modules.tenant.core.dto.TenantResponse;
import com.chatbot.modules.tenant.core.mapper.TenantMapper;
import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.repository.TenantRepository;
import com.chatbot.modules.tenant.membership.model.TenantMember;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.modules.auth.repository.AuthRepository;

import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMemberRepository tenantMemberRepository;
    private final AuthRepository authRepository;

    public TenantService(TenantRepository tenantRepository, TenantMemberRepository tenantMemberRepository, AuthRepository authRepository) {
        this.tenantRepository = tenantRepository;
        this.tenantMemberRepository = tenantMemberRepository;
        this.authRepository = authRepository;
    }

    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        // 1. Lấy thông tin user hiện tại từ SecurityContext
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Auth currentUser = authRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Map từ request sang entity
        Tenant tenant = TenantMapper.toEntity(request);
        Tenant savedTenant = tenantRepository.save(tenant);

        // 3. Tạo Member
        TenantMember member = TenantMember.builder()
                .tenant(savedTenant)
                .user(currentUser)
                .role(TenantRole.OWNER)
                .build();
        tenantMemberRepository.save(member);

        // 4. Trả về Response DTO
        return TenantMapper.toResponse(savedTenant);
    }
        
    @Transactional(readOnly = true)
    public Tenant getTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    @Transactional
    public void suspendTenant(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        tenant.setStatus(TenantStatus.SUSPENDED);
    }

    @Transactional
    public void deactivateTenant(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        tenant.setStatus(TenantStatus.INACTIVE);
    }

    @Transactional
    public void activateTenant(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        tenant.setStatus(TenantStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Tenant> getUserTenants() {
        // Lấy email người dùng hiện tại từ SecurityContext
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Sử dụng phương thức mới để lấy danh sách thành viên cùng với thông tin tenant
        return tenantMemberRepository.findByUserEmailWithTenant(currentUserEmail).stream()
                .map(TenantMember::getTenant)
                .collect(Collectors.toList());
    }

    // Lấy thông tin Tenant và kiểm tra xem User hiện tại có quyền truy cập không
    @Transactional(readOnly = true)
    public TenantResponse getTenantForCurrentUser(Long tenantId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Kiểm tra xem user có phải là member của tenant này không
        TenantMember member = tenantMemberRepository.findByTenantIdAndUserEmail(tenantId, currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Bạn không có quyền truy cập Tenant này hoặc Tenant không tồn tại"));

        return TenantMapper.toResponse(member.getTenant());
    }

    // Logic Switch Tenant
    @Transactional
    public TenantResponse switchTenant(Long tenantId) {
        // 1. Kiểm tra quyền truy cập tương tự hàm lấy thông tin
        TenantResponse tenantResponse = getTenantForCurrentUser(tenantId);
        
        // 2. Tại đây bạn có thể thêm logic bổ sung như:
        // - Cập nhật 'last_accessed_at' của member
        // - Ghi Log hoạt động chuyển đổi workspace
        
        return tenantResponse;
    }
}
