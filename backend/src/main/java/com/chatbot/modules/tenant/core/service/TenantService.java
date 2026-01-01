package com.chatbot.modules.tenant.core.service;

import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.auth.repository.AuthRepository;
import com.chatbot.modules.tenant.membership.model.MembershipStatus;
import com.chatbot.modules.auth.model.SystemRole;
import com.chatbot.modules.tenant.core.dto.*;
import com.chatbot.modules.tenant.core.mapper.TenantMapper;
import com.chatbot.modules.tenant.core.model.*;
import com.chatbot.modules.tenant.core.repository.TenantRepository;
import com.chatbot.modules.tenant.membership.model.TenantMember;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;

import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMemberRepository tenantMemberRepository;
    private final AuthRepository authRepository;

    public TenantService(
            TenantRepository tenantRepository,
            TenantMemberRepository tenantMemberRepository,
            AuthRepository authRepository) {
        this.tenantRepository = tenantRepository;
        this.tenantMemberRepository = tenantMemberRepository;
        this.authRepository = authRepository;
    }

    /**
     * Tạo tenant mới và gán user hiện tại làm OWNER.
     */
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        String currentUserEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();

        Auth currentUser = authRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set default visibility to PUBLIC if not provided
        if (request.getVisibility() == null) {
            request.setVisibility(TenantVisibility.PUBLIC);
        }

        Tenant tenant = TenantMapper.toEntity(request);
        Tenant savedTenant = tenantRepository.save(tenant);

        TenantMember owner = TenantMember.builder()
                .tenant(savedTenant)
                .user(currentUser)
                .role(TenantRole.OWNER)
                .status(MembershipStatus.ACTIVE)
                .build();

        tenantMemberRepository.save(owner);

        return TenantMapper.toResponse(savedTenant);
    }

    /**
     * Lấy tenant theo ID (internal use).
     */
    @Transactional(readOnly = true)
    public Tenant getTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    /**
     * Suspend tenant.
     */
    @Transactional
    public void suspendTenant(Long tenantId) {
        // Lấy thông tin tenant
        Tenant tenant = getTenant(tenantId);
        
        // Kiểm tra quyền admin
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = authRepository.findByEmail(currentUserEmail)
                .map(auth -> auth.getSystemRole() == SystemRole.ADMIN)
                .orElse(false);
                
        if (!isAdmin) {
            throw new RuntimeException("Chỉ admin mới có quyền tạm dừng tenant");
        }
        
        // Kiểm tra trạng thái hiện tại
        if (tenant.getStatus() == TenantStatus.SUSPENDED) {
            throw new RuntimeException("Tenant đã ở trạng thái tạm dừng");
        }
        
        // Thực hiện cập nhật
        tenant.setStatus(TenantStatus.SUSPENDED);
        tenant.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Deactivate tenant.
     */
    @Transactional
    public void deactivateTenant(Long tenantId) {
        // Lấy thông tin tenant
        Tenant tenant = getTenant(tenantId);
        
        // Kiểm tra quyền owner
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isOwner = tenantMemberRepository.existsByTenantIdAndUserEmailAndRoleAndStatus(
                tenantId, 
                currentUserEmail, 
                TenantRole.OWNER,
                MembershipStatus.ACTIVE
        );
        
        if (!isOwner) {
            throw new RuntimeException("Chỉ chủ sở hữu mới có quyền vô hiệu hóa tenant");
        }
        
        // Kiểm tra trạng thái hiện tại
        if (tenant.getStatus() == TenantStatus.INACTIVE) {
            throw new RuntimeException("Tenant đã ở trạng thái không hoạt động");
        }
        
        // Thực hiện cập nhật
        tenant.setStatus(TenantStatus.INACTIVE);
        tenant.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Activate tenant.
     */
    @Transactional
    public void activateTenant(Long tenantId) {
        // Lấy thông tin tenant
        Tenant tenant = getTenant(tenantId);
        
        // Kiểm tra quyền admin
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = authRepository.findByEmail(currentUserEmail)
                .map(auth -> auth.getSystemRole() == SystemRole.ADMIN)
                .orElse(false);
                
        if (!isAdmin) {
            throw new RuntimeException("Chỉ admin mới có quyền kích hoạt tenant");
        }
        
        // Kiểm tra trạng thái hiện tại
        if (tenant.getStatus() == TenantStatus.ACTIVE) {
            throw new RuntimeException("Tenant đã ở trạng thái hoạt động");
        }
        
        // Thực hiện cập nhật
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Lấy danh sách tenant mà user hiện tại là member.
     */
    @Transactional(readOnly = true)
    public List<Tenant> getUserTenants() {
        String currentUserEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();

        return tenantMemberRepository
                .findByUserEmailWithTenant(currentUserEmail)
                .stream()
                .map(TenantMember::getTenant)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin tenant nếu user hiện tại là member.
     */
    @Transactional(readOnly = true)
    public TenantResponse getTenantForCurrentUser(Long tenantId) {
        String currentUserEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();

        // Chỉ cho phép truy cập nếu thành viên có trạng thái ACTIVE
        TenantMember member =
                tenantMemberRepository
                        .findByTenantIdAndUserEmailAndStatus(
                                tenantId, 
                                currentUserEmail,
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException("Bạn không có quyền truy cập tenant này"));

        return TenantMapper.toResponse(member.getTenant());
    }

    /**
     * Switch tenant (validate membership).
     */
    @Transactional
    public TenantResponse switchTenant(Long tenantId) {
        return getTenantForCurrentUser(tenantId);
    }

    /**
     * Search tenant theo keyword + paging.
     * Trả thêm cờ isMember cho FE.
     */
        @Transactional(readOnly = true)
        public Page<TenantSearchResponse> searchTenants(
                TenantSearchRequest request,
                String currentUserEmail) {

        // 1. Tìm kiếm danh sách Tenant theo phân trang
        Page<Tenant> tenantsPage = tenantRepository.findByVisibilityAndStatusAndNameContainingIgnoreCase(
                TenantVisibility.PUBLIC, 
                TenantStatus.ACTIVE, 
                request.getKeyword() != null ? request.getKeyword() : "",
                request.toPageable()
        );

        // 2. Lấy danh sách tất cả quan hệ hội viên của User này
        // Giả sử tenantMemberRepository có hàm tìm theo Email
        List<TenantMember> userMemberships = tenantMemberRepository.findByUserEmail(currentUserEmail);

        // 3. Map sang Response
        return tenantsPage.map(tenant -> {
                // Tìm xem tenant hiện tại có nằm trong danh sách hội viên của user không
                TenantMembershipStatus status = userMemberships.stream()
                        .filter(m -> m.getTenant().getId().equals(tenant.getId()))
                        .findFirst()
                        .map(m -> {
                        // Map từ MembershipStatus (của Entity) sang TenantMembershipStatus (của DTO)
                        if (m.getStatus() == MembershipStatus.PENDING) return TenantMembershipStatus.PENDING;
                        if (m.getStatus() == MembershipStatus.ACTIVE) return TenantMembershipStatus.APPROVED;
                        return TenantMembershipStatus.NONE;
                        })
                        .orElse(TenantMembershipStatus.NONE);

                return TenantSearchResponse.builder()
                        .id(tenant.getId())
                        .name(tenant.getName())
                        .status(tenant.getStatus())
                        .visibility(tenant.getVisibility())
                        .createdAt(tenant.getCreatedAt())
                        .membershipStatus(status)
                        .build();
        });
}
}
