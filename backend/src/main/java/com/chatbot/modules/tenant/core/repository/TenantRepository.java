package com.chatbot.modules.tenant.core.repository;

import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.model.TenantVisibility;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    @Query("SELECT t FROM Tenant t WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Tenant> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT t FROM Tenant t WHERE " +
        "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "AND t.status = :status " +
        "AND t.visibility = :visibility")
    Page<Tenant> searchActivePublicTenants(
        @Param("keyword") String keyword, 
        @Param("status") TenantStatus status, 
        @Param("visibility") TenantVisibility visibility, 
        Pageable pageable);

    Page<Tenant> findByVisibilityAndStatusAndNameContainingIgnoreCase(
            TenantVisibility visibility, 
            TenantStatus status, 
            String name, 
            Pageable pageable
    );
}
