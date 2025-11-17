package com.chatbot.odoo.repository;

import com.chatbot.odoo.model.CustomerStatus;
import com.chatbot.odoo.model.FbCustomerStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface FbCustomerStagingRepository extends JpaRepository<FbCustomerStaging, String> {

    List<FbCustomerStaging> findByOwnerId(String ownerId);

    List<FbCustomerStaging> findByOwnerIdAndStatus(String ownerId, CustomerStatus status);

    Optional<FbCustomerStaging> findByPsidAndOwnerId(String psid, String ownerId);

    @Transactional // Để đảm bảo thao tác xóa được thực hiện đúng cách
    void deleteByPsidAndOwnerId(String psid, String ownerId);
}
