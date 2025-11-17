package com.chatbot.odoo.repository;

import com.chatbot.odoo.model.FbCapturedPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FbCapturedPhoneRepository extends JpaRepository<FbCapturedPhone, Long> {

    // Kiểm tra sự tồn tại theo số điện thoại
    boolean existsByPhoneNumber(String phoneNumber);

    // Kiểm tra sự tồn tại theo số điện thoại và ownerId
    boolean existsByPhoneNumberAndOwnerId(String phoneNumber, String ownerId);

    // Lấy danh sách các phone theo ownerId
    List<FbCapturedPhone> findByOwnerId(String ownerId);
}
