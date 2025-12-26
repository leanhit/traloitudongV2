package com.chatbot.modules.tenant.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private String id;
    private BigDecimal amount;
    private String currency;
    
    // Sử dụng String để dễ dàng trả về cho Frontend
    private String status; 
    
    // Khớp với Invoice.invoiceUrl
    private String invoicePdf; 
    
    // Khớp với Invoice.receiptUrl (nếu cần hiển thị)
    private String receiptUrl; 

    // Khớp với Invoice.issueDate
    private LocalDateTime createdAt; 
    
    private LocalDateTime dueDate;
    private LocalDateTime paidDate;
    private String description;
}