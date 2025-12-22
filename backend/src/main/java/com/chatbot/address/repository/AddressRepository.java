package com.chatbot.address.repository;

import com.chatbot.address.model.Address;
import com.chatbot.address.model.OwnerType;
import com.chatbot.userInfo.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByOwnerTypeAndUser(OwnerType ownerType, UserInfo user);
    
    // Tìm địa chỉ mặc định của một user/store để xử lý logic khi update
    Optional<Address> findByOwnerTypeAndUserAndIsDefaultTrue(OwnerType ownerType, UserInfo user);
}