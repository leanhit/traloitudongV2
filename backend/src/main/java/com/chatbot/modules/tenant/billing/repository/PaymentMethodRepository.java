// PaymentMethodRepository.java

package com.chatbot.modules.tenant.billing.repository;

import com.chatbot.modules.tenant.billing.model.PaymentMethod;

import com.chatbot.modules.tenant.core.model.Tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByTenant(Tenant tenant);
    
    @Query("SELECT p FROM PaymentMethod p WHERE p.tenant.id = :tenantId")
    List<PaymentMethod> findByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT p FROM PaymentMethod p WHERE p.tenant.id = :tenantId")
    Page<PaymentMethod> findByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM PaymentMethod p WHERE p.tenant.id = :tenantId")
    long countByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT p FROM PaymentMethod p WHERE p.id = :id AND p.tenant.id = :tenantId")
    Optional<PaymentMethod> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
    
    @Query("SELECT p FROM PaymentMethod p WHERE p.tenant.id = :tenantId AND p.isDefault = true")
    Optional<PaymentMethod> findByTenantIdAndIsDefaultTrue(@Param("tenantId") Long tenantId);
    
    Optional<PaymentMethod> findByIdAndTenant(Long id, Tenant tenant);
    Optional<PaymentMethod> findByGatewayId(String gatewayId);
    Optional<PaymentMethod> findByTenantAndIsDefaultTrue(Tenant tenant);

}