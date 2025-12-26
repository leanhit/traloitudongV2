// BillingController.java
package com.chatbot.modules.tenant.billing.controller;

import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.tenant.billing.dto.*;
import com.chatbot.modules.tenant.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tenants/{tenantId}/billing")
@RequiredArgsConstructor
public class BillingController {
    
    private final BillingService billingService;
    
    // Subscription endpoints
    @GetMapping("/subscription")
    public SubscriptionResponse getCurrentSubscription(
            @PathVariable Long tenantId,
            @AuthenticationPrincipal Auth currentUser) {
        return billingService.getCurrentSubscription(tenantId);
    }
    
    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @PathVariable Long tenantId,
            @Valid @RequestBody SubscriptionRequest request,
            @AuthenticationPrincipal Auth currentUser) {
        return ResponseEntity.ok(billingService.subscribe(tenantId, request, currentUser));
    }
    
    @PostMapping("/cancel-subscription")
    public ResponseEntity<Void> cancelSubscription(
            @PathVariable Long tenantId,
            @AuthenticationPrincipal Auth currentUser) {
        billingService.cancelSubscription(tenantId, currentUser);
        return ResponseEntity.accepted().build();
    }
    
    // Invoice endpoints
    @GetMapping("/invoices")
    public Page<InvoiceResponse> listInvoices(
            @PathVariable Long tenantId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Auth currentUser) {
        return billingService.listInvoices(tenantId, pageable);
    }
    
    @GetMapping("/invoices/{invoiceId}")
    public InvoiceResponse getInvoice(
            @PathVariable Long tenantId,
            @PathVariable String invoiceId,
            @AuthenticationPrincipal Auth currentUser) {
        return billingService.getInvoice(tenantId, invoiceId);
    }
    
    @GetMapping("/invoices/{invoiceId}/pdf")
    public ResponseEntity<byte[]> getInvoicePdf(
            @PathVariable Long tenantId,
            @PathVariable String invoiceId,
            @AuthenticationPrincipal Auth currentUser) {
        String pdfUrl = billingService.getInvoicePdf(tenantId, invoiceId);
        return ResponseEntity.status(302).header("Location", pdfUrl).build();
    }
    
    // Payment method endpoints
    @PostMapping("/payment-methods")
    public ResponseEntity<PaymentMethodResponse> addPaymentMethod(
            @PathVariable Long tenantId,
            @Valid @RequestBody PaymentMethodRequest request,
            @AuthenticationPrincipal Auth currentUser) {
        return ResponseEntity.ok(billingService.addPaymentMethod(tenantId, request, currentUser));
    }
    
    @DeleteMapping("/payment-methods/{methodId}")
    public ResponseEntity<Void> removePaymentMethod(
            @PathVariable Long tenantId,
            @PathVariable Long methodId,
            @AuthenticationPrincipal Auth currentUser) {
        billingService.removePaymentMethod(tenantId, methodId, currentUser);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/payment-methods/{methodId}/default")
    public ResponseEntity<Void> setDefaultPaymentMethod(
            @PathVariable Long tenantId,
            @PathVariable Long methodId,
            @AuthenticationPrincipal Auth currentUser) {
        billingService.setDefaultPaymentMethod(tenantId, methodId, currentUser);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/payment-methods")
    public Page<PaymentMethodResponse> listPaymentMethods(
            @PathVariable Long tenantId,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal Auth currentUser) {
        return billingService.listPaymentMethods(tenantId, pageable);
    }
    
    // Webhook endpoint for payment gateway callbacks
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @PathVariable Long tenantId,
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        billingService.handleWebhookEvent(payload, signature);
        return ResponseEntity.ok().build();
    }
}