package com.chatbot.address.dto;

import lombok.Data;

@Data
public class AddressResponseDTO {
    private Long id;
    private String fullAddress; // Ví dụ: "Số 10, Đường ABC, Phường X, Hà Nội"
    private boolean isDefault;
    
    // Bạn có thể chọn ẩn ownerId/ownerType ở bản tin trả về nếu không cần thiết
}