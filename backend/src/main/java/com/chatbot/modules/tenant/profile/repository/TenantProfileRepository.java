package com.chatbot.modules.tenant.profile.repository;

import com.chatbot.modules.tenant.profile.model.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantProfileRepository
        extends JpaRepository<TenantProfile, Long> {

    Optional<TenantProfile> findByTenant_Id(Long tenantId);
}
