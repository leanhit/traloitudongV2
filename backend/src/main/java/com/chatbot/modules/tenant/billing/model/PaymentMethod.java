// PaymentMethod.java

package com.chatbot.modules.tenant.billing.model;

import com.chatbot.modules.tenant.core.model.Tenant;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity

@Table(name = "payment_methods")

@Getter @Setter

@NoArgsConstructor

@AllArgsConstructor

@Builder

public class PaymentMethod {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

   

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "tenant_id", nullable = false)

    private Tenant tenant;

   

    @Enumerated(EnumType.STRING)

    private PaymentMethodType type;

   

    private String gatewayId;

    private String lastFour;

    private String brand;

    private Integer expiryMonth;

    private Integer expiryYear;

    private boolean isDefault;

   

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

   

    @PrePersist

    protected void onCreate() {

        createdAt = LocalDateTime.now();

        updatedAt = LocalDateTime.now();

    }

   

    @PreUpdate

    protected void onUpdate() {

        updatedAt = LocalDateTime.now();

    }

}