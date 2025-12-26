package com.chatbot.modules.address.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.modules.address.dto.*;
import com.chatbot.modules.address.mapper.AddressMapper;
import com.chatbot.modules.address.model.Address;
import com.chatbot.modules.address.model.OwnerType;
import com.chatbot.modules.address.repository.AddressRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Transactional
    public AddressResponseDTO createAddress(Long tenantId, AddressRequestDTO dto) {
        if (dto.isDefault()) {
            resetDefault(tenantId, dto.getOwnerType(), dto.getOwnerId());
        }
        Address address = addressMapper.toEntity(tenantId, dto);
        return addressMapper.toResponseDTO(addressRepository.save(address));
    }

    public List<AddressResponseDTO> getAddressesByOwner(Long tenantId, OwnerType type, Long ownerId) {
        return addressRepository
                .findByTenantIdAndOwnerTypeAndOwnerId(tenantId, type, ownerId)
                .stream()
                .map(addressMapper::toResponseDTO)
                .toList();
    }

    public AddressDetailResponseDTO getAddressDetail(Long tenantId, Long id) {
        Address address = addressRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        return addressMapper.toDetailDTO(address);
    }

    @Transactional
    public AddressResponseDTO updateAddress(Long tenantId, Long id, AddressRequestDTO dto) {
        Address address = addressRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (dto.isDefault() && !address.isDefault()) {
            resetDefault(tenantId, address.getOwnerType(), address.getOwnerId());
        }

        address.setHouseNumber(dto.getHouseNumber());
        address.setStreet(dto.getStreet());
        address.setWard(dto.getWard());
        address.setCity(dto.getCity());
        address.setDefault(dto.isDefault());

        return addressMapper.toResponseDTO(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long tenantId, Long id) {
        Address address = addressRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        addressRepository.delete(address);
    }

    @Transactional
    public void createEmptyAddressForUser(Long userId) {
        // 1. Khởi tạo DTO
        AddressRequestDTO emptyAddress = new AddressRequestDTO();
        emptyAddress.setOwnerType(OwnerType.USER);
        emptyAddress.setOwnerId(userId);
        emptyAddress.setDefault(true);
        emptyAddress.setStreet("");
        emptyAddress.setHouseNumber("");
        emptyAddress.setWard("");
        emptyAddress.setCity("");

        // 2. Thực hiện lưu
        try {
            // Sử dụng tenantId mặc định là 1 (hoặc lấy từ cấu hình hệ thống)
            createAddress(1L, emptyAddress);
            log.info("Đã tạo địa chỉ trống mặc định cho user ID: {}", userId);
        } catch (Exception e) {
            // Log lỗi để debug nhưng không làm crash quá trình đăng ký chính
            log.error("Lỗi khi tạo địa chỉ mặc định cho user {}: {}", userId, e.getMessage());
        }
    }

    private void resetDefault(Long tenantId, OwnerType type, Long ownerId) {
        addressRepository
                .findByTenantIdAndOwnerTypeAndOwnerIdAndIsDefaultTrue(tenantId, type, ownerId)
                .ifPresent(addr -> {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                });
    }
}