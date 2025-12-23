package com.chatbot.address.repository;

import com.chatbot.address.model.Address;
import com.chatbot.address.model.OwnerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    // 1. Sửa lỗi: findByTenantIdAndOwnerTypeAndOwnerId
    List<Address> findByTenantIdAndOwnerTypeAndOwnerId(Long tenantId, OwnerType ownerType, Long ownerId);

    // 2. Sửa lỗi: findByIdAndTenantId
    Optional<Address> findByIdAndTenantId(Long id, Long tenantId);

    // 3. Sửa lỗi: findByTenantIdAndOwnerTypeAndOwnerIdAndIsDefaultTrue
    Optional<Address> findByTenantIdAndOwnerTypeAndOwnerIdAndIsDefaultTrue(Long tenantId, OwnerType ownerType, Long ownerId);

    // Các phương thức bổ sung nếu cần
    List<Address> findByOwnerIdAndOwnerType(Long ownerId, OwnerType ownerType);
}