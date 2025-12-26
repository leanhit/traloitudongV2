package com.chatbot.modules.tenant.membership.controller;

import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.tenant.membership.dto.MemberResponse;
import com.chatbot.modules.tenant.membership.dto.UpdateMemberRoleRequest;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import com.chatbot.modules.tenant.membership.service.TenantMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tenants/{tenantId}/members")
@RequiredArgsConstructor
public class TenantMemberController {

    private final TenantMembershipService membershipService;

    @GetMapping
    public Page<MemberResponse> listMembers(
            @PathVariable Long tenantId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Auth currentUser) {
        // TODO: Kiểm tra quyền - chỉ ADMIN hoặc OWNER mới được xem danh sách
        return membershipService.listMembers(tenantId, pageable);
    }

    @GetMapping("/{userId}")
    public MemberResponse getMember(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @AuthenticationPrincipal Auth currentUser) {
        // TODO: Kiểm tra quyền - chỉ xem được thông tin của chính mình hoặc ADMIN/OWNER
        return membershipService.getMember(tenantId, userId);
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @AuthenticationPrincipal Auth currentUser) {
        // TODO: Kiểm tra quyền - chỉ ADMIN/OWNER mới được đổi role
        membershipService.updateMemberRole(tenantId, userId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @AuthenticationPrincipal Auth currentUser) {
        // TODO: Kiểm tra quyền - chỉ ADMIN/OWNER mới được xóa thành viên
        membershipService.removeMember(tenantId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invite")
    public ResponseEntity<Void> inviteMember(
            @PathVariable Long tenantId,
            @RequestParam String email,
            @RequestParam TenantRole role,
            @AuthenticationPrincipal Auth currentUser) {
        // TODO: Kiểm tra quyền - chỉ ADMIN/OWNER mới được mời thành viên
        membershipService.inviteByEmail(tenantId, email, role, currentUser);
        return ResponseEntity.accepted().build();
    }
}
