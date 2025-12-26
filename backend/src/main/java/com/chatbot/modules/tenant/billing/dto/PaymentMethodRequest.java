// PaymentMethodRequest.java

package com.chatbot.modules.tenant.billing.dto;



import com.chatbot.modules.tenant.billing.model.PaymentMethodType;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.NotBlank;



@Data

public class PaymentMethodRequest {

    @NotNull(message = "Payment method type is required")

    private PaymentMethodType type;

   

    @NotBlank(message = "Token is required")

    private String token;

   

    private boolean makeDefault = true;

}