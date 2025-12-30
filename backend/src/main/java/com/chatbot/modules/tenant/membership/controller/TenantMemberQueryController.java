package com.chatbot.modules.tenant.membership.controller;

import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.auth.security.CustomUserDetails;
import com.chatbot.modules.tenant.membership.dto.*;
import com.chatbot.modules.tenant.membership.service.TenantMembershipFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tenants/members")
public class TenantMemberQueryController {

    private final TenantMembershipFacade facade;

    @GetMapping("/pending-tenants")
    public List<TenantPendingResponse> myPendingTenants(
            @AuthenticationPrincipal CustomUserDetails customUser // Lấy CustomUserDetails
    ) {
        // Lấy Entity Auth từ bên trong CustomUserDetails
        Auth user = customUser.getAuth(); 
        return facade.myPending(user);
    }
}
