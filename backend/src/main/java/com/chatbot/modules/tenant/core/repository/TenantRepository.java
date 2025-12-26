package com.chatbot.modules.tenant.core.repository;

import com.chatbot.modules.tenant.core.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
