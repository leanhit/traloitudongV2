package com.chatbot.modules.tenant.membership.service;

import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.membership.dto.MemberResponse;
import com.chatbot.modules.tenant.membership.dto.UpdateMemberRoleRequest;
import com.chatbot.modules.tenant.membership.model.TenantMember;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantMembershipService {

    private final TenantMemberRepository memberRepo;
    
    /**
     * Kiểm tra xem user có phải là chủ sở hữu của tenant không
     */
    public boolean isTenantOwner(Long tenantId, Long userId) {
        return memberRepo.existsByTenant_IdAndUser_IdAndRole(tenantId, userId, TenantRole.OWNER);
    }

    /**
     * Gán OWNER khi tạo tenant
     */
    @Transactional
    public void assignOwner(Tenant tenant, Auth user) {
        if (memberRepo.existsByTenant_IdAndRole(
                tenant.getId(), TenantRole.OWNER)) {
            throw new IllegalStateException("Tenant already has OWNER");
        }

        TenantMember member = TenantMember.builder()
                .tenant(tenant)
                .user(user)
                .role(TenantRole.OWNER)
                .build();

        memberRepo.save(member);
    }

    /**
     * Invite user vào tenant
     */
    @Transactional
    public void addMember(
            Tenant tenant,
            Auth user,
            TenantRole role
    ) {
        memberRepo.findByTenant_IdAndUser_Id(
                tenant.getId(), user.getId()
        ).ifPresent(m -> {
            throw new IllegalStateException("User already in tenant");
        });

        TenantMember member = TenantMember.builder()
                .tenant(tenant)
                .user(user)
                .role(role)
                .build();

        memberRepo.save(member);
    }

    /**
     * Remove member (không được remove OWNER)
     */
    @Transactional
    public void removeMember(Long tenantId, Long userId) {
        TenantMember member = memberRepo
                .findByTenant_IdAndUser_Id(tenantId, userId)
                .orElseThrow(() -> new IllegalStateException("Member not found"));

        if (member.getRole() == TenantRole.OWNER) {
            throw new IllegalStateException("Cannot remove OWNER");
        }

        memberRepo.delete(member);
    }

    /**
     * Lấy danh sách thành viên có phân trang
     */
    public Page<MemberResponse> listMembers(Long tenantId, Pageable pageable) {
        return memberRepo.findByTenant_Id(tenantId, pageable)
                .map(this::toMemberResponse);
    }

    /**
     * Lấy thông tin chi tiết 1 thành viên
     */
    public MemberResponse getMember(Long tenantId, Long userId) {
        return memberRepo.findByTenant_IdAndUser_Id(tenantId, userId)
                .map(this::toMemberResponse)
                .orElseThrow(() -> new IllegalStateException("Member not found"));
    }

    /**
     * Cập nhật vai trò thành viên
     */
    @Transactional
    public void updateMemberRole(Long tenantId, Long userId, UpdateMemberRoleRequest request) {
        TenantMember member = memberRepo.findByTenant_IdAndUser_Id(tenantId, userId)
                .orElseThrow(() -> new IllegalStateException("Member not found"));

        // Không cho thay đổi role của OWNER
        if (member.getRole() == TenantRole.OWNER) {
            throw new IllegalStateException("Cannot change OWNER role");
        }

        member.setRole(request.getRole());
        memberRepo.save(member);
    }

    /**
     * Mời thành viên bằng email
     */
    @Transactional
    public void inviteByEmail(Long tenantId, String email, TenantRole role, Auth inviter) {
        // TODO: Triển khai logic gửi email mời
        // 1. Tạo token mời
        // 2. Lưu vào bảng invitation
        // 3. Gửi email chứa link kích hoạt
        throw new UnsupportedOperationException("Email invitation not implemented yet");
    }

    private MemberResponse toMemberResponse(TenantMember member) {
        return MemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .email(member.getUser().getEmail())
                .role(member.getRole())
                .joinedAt(member.getCreatedAt())
                .build();
    }
}
