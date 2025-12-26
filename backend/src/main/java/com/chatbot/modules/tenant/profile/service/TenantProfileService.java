package com.chatbot.modules.tenant.profile.service;

import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.profile.dto.TenantProfileRequest;
import com.chatbot.modules.tenant.profile.dto.TenantProfileResponse;
import com.chatbot.modules.tenant.profile.model.TenantProfile;
import com.chatbot.modules.tenant.profile.repository.TenantProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantProfileService {

    private final TenantProfileRepository repo;

    @Transactional
    public TenantProfileResponse getProfile(Long tenantId) {
        TenantProfile profile = repo.findByTenant_Id(tenantId)
                .orElseThrow(() ->
                        new IllegalStateException("Tenant profile not found"));

        return map(profile);
    }

    @Transactional
    public TenantProfileResponse upsertProfile(
            Tenant tenant,
            TenantProfileRequest req
    ) {
        TenantProfile profile = repo.findByTenant_Id(tenant.getId())
                .orElse(
                        TenantProfile.builder()
                                .tenant(tenant)
                                .build()
                );

        // update fields
        profile.setLegalName(req.getLegalName());
        profile.setTaxCode(req.getTaxCode());
        profile.setAddress(req.getAddress());
        profile.setContactEmail(req.getContactEmail());
        profile.setContactPhone(req.getContactPhone());
        profile.setLogoUrl(req.getLogoUrl());
        profile.setFaviconUrl(req.getFaviconUrl());
        profile.setPrimaryColor(req.getPrimaryColor());

        repo.save(profile);
        return map(profile);
    }

    private TenantProfileResponse map(TenantProfile p) {
        return TenantProfileResponse.builder()
                .tenantId(p.getTenant().getId())
                .legalName(p.getLegalName())
                .taxCode(p.getTaxCode())
                .address(p.getAddress())
                .contactEmail(p.getContactEmail())
                .contactPhone(p.getContactPhone())
                .logoUrl(p.getLogoUrl())
                .faviconUrl(p.getFaviconUrl())
                .primaryColor(p.getPrimaryColor())
                .build();
    }
}
