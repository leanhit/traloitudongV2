package com.chatbot.address.mapper;

import com.chatbot.address.dto.*;
import com.chatbot.address.model.Address;
import com.chatbot.userInfo.model.UserInfo;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressRequestDTO dto, UserInfo user) {
        return Address.builder()
                .ownerType(dto.getOwnerType())
                .user(user)
                .houseNumber(dto.getHouseNumber())
                .street(dto.getStreet())
                .ward(dto.getWard())
                .city(dto.getCity())
                .isDefault(dto.isDefault())
                .country("Vietnam") // Default value
                .build();
    }

    public AddressResponseDTO toResponseDTO(Address entity) {
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setId(entity.getId());
        dto.setFullAddress(buildFullAddress(entity));
        dto.setDefault(entity.isDefault());
        return dto;
    }

    public AddressDetailResponseDTO toDetailDTO(Address entity) {
        AddressDetailResponseDTO dto = new AddressDetailResponseDTO();
        dto.setId(entity.getId());
        dto.setOwnerType(entity.getOwnerType());
        dto.setOwnerId(entity.getUser() != null ? entity.getUser().getId() : null);
        dto.setHouseNumber(entity.getHouseNumber());
        dto.setStreet(entity.getStreet());
        dto.setWard(entity.getWard());
        dto.setDistrict(entity.getDistrict());
        dto.setCity(entity.getCity());
        dto.setFullAddress(buildFullAddress(entity));
        dto.setDefault(entity.isDefault());
        return dto;
    }

    private String buildFullAddress(Address entity) {
        return String.format("%s %s, %s, %s", 
            entity.getHouseNumber(), entity.getStreet(), entity.getWard(), entity.getCity());
    }
}