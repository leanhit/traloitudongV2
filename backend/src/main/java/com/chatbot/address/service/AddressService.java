package com.chatbot.address.service;

import com.chatbot.address.dto.*;
import com.chatbot.address.mapper.AddressMapper;
import com.chatbot.address.model.Address;
import com.chatbot.address.model.OwnerType;
import com.chatbot.address.repository.AddressRepository;
import com.chatbot.userInfo.model.UserInfo;
import com.chatbot.userInfo.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final UserInfoRepository userInfoRepository;

    @Transactional
    public AddressResponseDTO createAddress(AddressRequestDTO dto) {
        UserInfo user = userInfoRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (dto.isDefault()) {
            resetDefaultAddress(dto.getOwnerType(), user.getId());
        }
        
        Address address = addressMapper.toEntity(dto, user);
        return addressMapper.toResponseDTO(addressRepository.save(address));
    }

    public List<AddressResponseDTO> getAddressesByOwner(OwnerType type, Long id) {
        UserInfo user = userInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        return addressRepository.findByOwnerTypeAndUser(type, user)
                .stream()
                .map(addressMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public AddressDetailResponseDTO getAddressDetail(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        return addressMapper.toDetailDTO(address);
    }

    @Transactional
    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO dto) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (dto.isDefault() && !address.isDefault()) {
            resetDefaultAddress(address.getOwnerType(), address.getUser().getId());
        }

        address.setHouseNumber(dto.getHouseNumber());
        address.setStreet(dto.getStreet());
        address.setWard(dto.getWard());
        address.setCity(dto.getCity());
        address.setDefault(dto.isDefault());

        return addressMapper.toResponseDTO(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    private void resetDefaultAddress(OwnerType type, Long userId) {
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        addressRepository.findByOwnerTypeAndUserAndIsDefaultTrue(type, user)
                .ifPresent(addr -> {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                });
    }
}