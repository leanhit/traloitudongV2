package com.chatbot.modules.tenant.membership.service;

import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.tenant.membership.dto.*;
import com.chatbot.modules.tenant.membership.model.*;
import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantSelfService {

    private final TenantMemberRepository memberRepo;

    /* ================= MY PENDING ================= */

    public List<TenantPendingResponse> getMyPending(Auth user) {
        return memberRepo.findByUser_IdAndStatus(user.getId(), MembershipStatus.PENDING)
                .stream()
                .map(m -> TenantPendingResponse.builder()
                        .id(m.getTenant().getId())
                        .name(m.getTenant().getName())
                        .description(m.getTenant().getDescription())
                        .requestedAt(m.getCreatedAt())
                        .build())
                .toList();
    }

    /* ================= LEAVE ================= */

    @Transactional
    public void leaveTenant(Long tenantId, Auth user) {
        TenantMember member = memberRepo.findByTenant_IdAndUser_Id(tenantId, user.getId())
                .orElseThrow(() -> new IllegalStateException("Not a member"));

        if (member.getRole() == TenantRole.OWNER) {
            throw new IllegalStateException("OWNER must transfer ownership first");
        }

        memberRepo.delete(member);
    }
}
