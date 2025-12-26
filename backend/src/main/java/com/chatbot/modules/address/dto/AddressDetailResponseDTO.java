package com.chatbot.modules.address.dto;

import com.chatbot.modules.address.model.OwnerType;

import lombok.Data;

@Data
public class AddressDetailResponseDTO {
    private Long id;
    private OwnerType ownerType;
    private Long ownerId;

    private String houseNumber;
    private String street;
    private String ward;
    private String district;
    private String city;

    private String fullAddress;
    private boolean isDefault;
}
