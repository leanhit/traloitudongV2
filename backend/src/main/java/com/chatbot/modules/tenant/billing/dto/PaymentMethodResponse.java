// PaymentMethodResponse.java
package com.chatbot.modules.tenant.billing.dto;

import com.chatbot.modules.tenant.billing.model.PaymentMethodType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentMethodResponse {

    private Long id;

    private PaymentMethodType type;

    private String lastFour;

    private String brand;

    private Integer expiryMonth;

    private Integer expiryYear;

    private boolean isDefault;

    private LocalDateTime createdAt;

}