// SubscriptionResponse.java

package com.chatbot.modules.tenant.billing.dto;



import com.chatbot.modules.tenant.billing.model.BillingPlan;

import com.chatbot.modules.tenant.billing.model.SubscriptionStatus;

import lombok.Builder;

import lombok.Data;

import java.time.LocalDateTime;

@Data

@Builder

public class SubscriptionResponse {

    private Long id;

    private BillingPlan plan;

    private SubscriptionStatus status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private boolean autoRenew;

    private String planName;

    private Double price;

    private Integer maxMessages;

    private Integer maxChatbots;

}