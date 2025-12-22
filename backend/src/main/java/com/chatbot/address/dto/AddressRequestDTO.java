package com.chatbot.address.dto;

import com.chatbot.address.model.OwnerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressRequestDTO {
    @NotNull(message = "Owner type is required")
    private OwnerType ownerType;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @NotBlank(message = "Street cannot be empty")
    private String street;

    private String houseNumber;
    
    @NotBlank(message = "Ward is required")
    private String ward;

    @NotBlank(message = "City is required")
    private String city;

    private boolean isDefault;
}