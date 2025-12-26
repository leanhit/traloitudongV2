// Invoice.java
package com.chatbot.modules.tenant.billing.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.chatbot.modules.tenant.core.model.Tenant;

@Entity
@Table(name = "invoices")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id
    private String id;   

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private Tenant tenant;

    private BigDecimal amount;
    private String currency;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private LocalDateTime paidDate;   

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;   

    private String invoiceUrl;
    private String receiptUrl;   

    @Column(columnDefinition = "TEXT")
    private String description;
}