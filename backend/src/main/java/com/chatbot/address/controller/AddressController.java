package com.chatbot.address.controller;

import com.chatbot.address.dto.*;
import com.chatbot.address.model.OwnerType;
import com.chatbot.address.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponseDTO> create(@Valid @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.ok(addressService.createAddress(dto));
    }

    @GetMapping("/owner/{type}/{id}")
    public ResponseEntity<List<AddressResponseDTO>> getByOwner(
            @PathVariable OwnerType type, 
            @PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressesByOwner(type, id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDetailResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressDetail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> update(
            @PathVariable Long id, 
            @Valid @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.ok(addressService.updateAddress(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}