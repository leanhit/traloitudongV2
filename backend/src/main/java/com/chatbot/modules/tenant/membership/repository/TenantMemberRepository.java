package com.chatbot.modules.tenant.membership.repository;

import com.chatbot.modules.tenant.membership.model.TenantMember;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TenantMemberRepository
        extends JpaRepository<TenantMember, Long> {

    Optional<TenantMember> findByTenant_IdAndUser_Id(Long tenantId, Long userId);

    List<TenantMember> findByTenant_Id(Long tenantId);

    boolean existsByTenant_IdAndRole(Long tenantId, TenantRole role);
    
    boolean existsByTenant_IdAndUser_IdAndRole(Long tenantId, Long userId, TenantRole role);
    
    Page<TenantMember> findByTenant_Id(Long tenantId, Pageable pageable);

    List<TenantMember> findByUserEmail(String email);
    
    @Query("SELECT tm FROM TenantMember tm JOIN FETCH tm.tenant WHERE tm.user.email = :email")
    List<TenantMember> findByUserEmailWithTenant(@Param("email") String email);
    
    // Tìm member theo ID tenant và Email user
    @Query("SELECT tm FROM TenantMember tm JOIN FETCH tm.tenant WHERE tm.tenant.id = :tenantId AND tm.user.email = :userEmail")
    Optional<TenantMember> findByTenantIdAndUserEmail(Long tenantId, String userEmail);
}
