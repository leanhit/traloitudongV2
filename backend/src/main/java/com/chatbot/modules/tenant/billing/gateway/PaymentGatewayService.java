//PaymentGatewayService.java
package com.chatbot.modules.tenant.billing.gateway;



public interface PaymentGatewayService {

    String charge(
            Long tenantId,
            long amount,
            String currency,
            String description
    );

    String createPaymentIntent(
            double amount,
            String currency,
            String description
    );
}