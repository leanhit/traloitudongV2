package com.chatbot.address.dto;

import com.chatbot.address.model.OwnerType;
import lombok.Data;

@Data
public class AddressDetailResponseDTO {
    private Long id;
    private OwnerType ownerType;
    private Long ownerId;
    
    // Tách chi tiết để FE hiển thị lên Form Edit
    private String houseNumber;
    private String street;
    private String ward;
    private String district;
    private String city;
    
    private String fullAddress; // Chuỗi gộp để hiển thị nhanh
    private boolean isDefault;
}