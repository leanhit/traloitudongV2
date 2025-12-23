package com.chatbot.address.mapper;

import com.chatbot.address.dto.*;
import com.chatbot.address.model.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(Long tenantId, AddressRequestDTO dto) {
        return Address.builder()
                .tenantId(tenantId)
                .ownerType(dto.getOwnerType())
                .ownerId(dto.getOwnerId())
                .houseNumber(dto.getHouseNumber())
                .street(dto.getStreet())
                .ward(dto.getWard())
                .city(dto.getCity())
                .isDefault(dto.isDefault())
                .country("Vietnam")
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
        dto.setOwnerId(entity.getOwnerId());
        dto.setHouseNumber(entity.getHouseNumber());
        dto.setStreet(entity.getStreet());
        dto.setWard(entity.getWard());
        dto.setDistrict(entity.getDistrict());
        dto.setCity(entity.getCity());
        dto.setFullAddress(buildFullAddress(entity));
        dto.setDefault(entity.isDefault());
        return dto;
    }

    private String buildFullAddress(Address e) {
        return String.format("%s %s, %s, %s",
                e.getHouseNumber() == null ? "" : e.getHouseNumber(),
                e.getStreet(),
                e.getWard(),
                e.getCity());
    }
}
