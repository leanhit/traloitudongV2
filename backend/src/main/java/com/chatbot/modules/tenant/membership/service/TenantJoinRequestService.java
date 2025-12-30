package com.chatbot.modules.tenant.membership.service;

import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.core.repository.TenantRepository;
import com.chatbot.modules.tenant.membership.dto.*;
import com.chatbot.modules.tenant.membership.model.*;
import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantJoinRequestService {

    private final TenantMemberRepository memberRepo;
    private final TenantRepository tenantRepo;

    /* ================= REQUEST ================= */

    @Transactional
    public void requestToJoin(Long tenantId, Auth user) {
        memberRepo.findByTenant_IdAndUser_Id(tenantId, user.getId())
                .ifPresent(m -> {
                    throw new IllegalStateException("Already requested or member");
                });

        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found"));

        memberRepo.save(TenantMember.builder()
                .tenant(tenant)
                .user(user)
                .role(TenantRole.NONE)
                .status(MembershipStatus.PENDING)
                .build());
    }

    /* ================= LIST ================= */

    public List<MemberResponse> getPendingRequests(Long tenantId) {
        return memberRepo.findByTenant_IdAndStatus(tenantId, MembershipStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ================= UPDATE (SPEC) ================= */
    /**
     * PATCH /join-requests/{id}
     * { status: APPROVED | REJECTED }
     */
    @Transactional
    public void updateStatus(Long tenantId, Long requestId, MembershipStatus status) {
        TenantMember member = getPending(tenantId, requestId);

        if (status == MembershipStatus.ACTIVE) {
            member.setStatus(MembershipStatus.ACTIVE);
            member.setRole(TenantRole.MEMBER); 
            member.setJoinedAt(LocalDateTime.now());
        } else if (status == MembershipStatus.REJECTED) {
            memberRepo.delete(member);
        } else {
            throw new IllegalStateException("Invalid status");
        }
    }

    /* ================= HELPERS ================= */

    private TenantMember getPending(Long tenantId, Long memberId) {
        TenantMember m = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("Request not found"));

        if (!m.getTenant().getId().equals(tenantId)
                || m.getStatus() != MembershipStatus.PENDING) {
            throw new IllegalStateException("Invalid join request");
        }
        return m;
    }

    private MemberResponse toResponse(TenantMember m) {
        return MemberResponse.builder()
                .id(m.getId())
                .userId(m.getUser().getId())
                .email(m.getUser().getEmail())
                .role(m.getRole())
                .joinedAt(m.getJoinedAt() != null ? m.getJoinedAt() : m.getCreatedAt())
                .build();
    }
}
