// Subscription.java

package com.chatbot.modules.tenant.billing.model;

import com.chatbot.modules.tenant.core.model.Tenant;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity

@Table(name = "subscriptions")

@Getter @Setter

@NoArgsConstructor

@AllArgsConstructor

@Builder

public class Subscription {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

   

    @OneToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "tenant_id", nullable = false)

    private Tenant tenant;

   

    @Enumerated(EnumType.STRING)

    private BillingPlan plan;

   

    private LocalDateTime startDate;

    private LocalDateTime endDate;

   

    @Enumerated(EnumType.STRING)

    private SubscriptionStatus status;

   

    private String paymentGatewayId;

    private boolean autoRenew;

   

    @Version

    private Long version;

    // --- audit ---
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}