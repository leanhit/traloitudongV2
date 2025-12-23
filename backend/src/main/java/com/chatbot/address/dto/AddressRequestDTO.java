package com.chatbot.address.dto;

import com.chatbot.address.model.OwnerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressRequestDTO {

    @NotNull
    private OwnerType ownerType;

    @NotNull
    private Long ownerId;

    @NotBlank
    private String street;

    private String houseNumber;

    @NotBlank
    private String ward;

    @NotBlank
    private String city;

    private boolean isDefault;
}
