// SubscriptionRequest.java

package com.chatbot.modules.tenant.billing.dto;



import com.chatbot.modules.tenant.billing.model.BillingPlan;

import lombok.Data;

import jakarta.validation.constraints.NotNull;



@Data

public class SubscriptionRequest {

    @NotNull(message = "Billing plan is required")

    private BillingPlan plan;

   

    private String paymentMethodId;

    private String paymentToken;

    private boolean autoRenew = true;

}