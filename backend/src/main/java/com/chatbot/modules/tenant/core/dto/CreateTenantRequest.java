package com.chatbot.modules.tenant.core.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateTenantRequest {

    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
