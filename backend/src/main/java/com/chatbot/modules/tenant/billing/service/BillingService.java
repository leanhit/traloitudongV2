package com.chatbot.modules.tenant.billing.service;

import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.auth.model.SystemRole;
import com.chatbot.modules.tenant.billing.dto.*;
import com.chatbot.modules.tenant.billing.model.*;
import com.chatbot.modules.tenant.billing.repository.InvoiceRepository;
import com.chatbot.modules.tenant.billing.repository.PaymentMethodRepository;
import com.chatbot.modules.tenant.billing.repository.SubscriptionRepository;
import com.chatbot.modules.tenant.billing.gateway.PaymentGatewayService;

import com.chatbot.modules.tenant.billing.exception.ResourceNotFoundException;

import com.chatbot.modules.tenant.billing.exception.UnauthorizedException;

import com.chatbot.modules.tenant.core.model.Tenant;

import com.chatbot.modules.tenant.core.repository.TenantRepository;

import com.chatbot.modules.tenant.membership.service.TenantMembershipService;



import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.time.LocalDateTime;



@Slf4j

@Service

@RequiredArgsConstructor

public class BillingService {
    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final TenantMembershipService tenantMembershipService; // Đã bổ sung
    // --- Invoice Methods ---

    public InvoiceResponse getInvoice(Long tenantId, String invoiceId) {
        Tenant tenant = getTenant(tenantId);
        return invoiceRepository.findByIdAndTenant(invoiceId, tenant)
                .map(this::mapToInvoiceResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    public String getInvoicePdf(Long tenantId, String invoiceId) {
        Tenant tenant = getTenant(tenantId);
        // This would typically return a URL to the PDF or the PDF content
        // For now, we'll return a placeholder URL
        return "/api/tenants/" + tenantId + "/invoices/" + invoiceId + "/pdf";
    }

    // --- Payment Method Methods ---

    @Transactional
    public void removePaymentMethod(Long tenantId, Long methodId, Auth currentUser) {
        // Check permissions
        if (currentUser.getSystemRole() != SystemRole.ADMIN &&
            !tenantMembershipService.isTenantOwner(tenantId, currentUser.getId())) {
            throw new UnauthorizedException("No permission to manage payment methods for this tenant");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndTenantId(methodId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        // Prevent removing the default payment method if it's the only one
        if (paymentMethod.isDefault() && paymentMethodRepository.countByTenantId(tenantId) == 1) {
            throw new IllegalStateException("Cannot remove the only payment method");
        }

        paymentMethodRepository.delete(paymentMethod);
    }

    @Transactional
    public void setDefaultPaymentMethod(Long tenantId, Long methodId, Auth currentUser) {
        // Check permissions
        if (currentUser.getSystemRole() != SystemRole.ADMIN &&
            !tenantMembershipService.isTenantOwner(tenantId, currentUser.getId())) {
            throw new UnauthorizedException("No permission to manage payment methods for this tenant");
        }

        // Reset all payment methods to not default
        paymentMethodRepository.findByTenantId(tenantId).forEach(method -> {
            if (method.isDefault()) {
                method.setDefault(false);
                paymentMethodRepository.save(method);
            }
        });

        // Set the selected payment method as default
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndTenantId(methodId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
        
        paymentMethod.setDefault(true);
        paymentMethodRepository.save(paymentMethod);
    }

    public Page<PaymentMethodResponse> listPaymentMethods(Long tenantId, Pageable pageable) {
        return paymentMethodRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToPaymentMethodResponse);
    }

    // --- Subscription Methods ---



    @Transactional(readOnly = true)

    public SubscriptionResponse getCurrentSubscription(Long tenantId) {

        Tenant tenant = getTenant(tenantId);

        Subscription subscription = subscriptionRepository.findByTenant(tenant)

                .orElseThrow(() -> new ResourceNotFoundException("No subscription found for tenant"));

       

        return mapToSubscriptionResponse(subscription);

    }



    @Transactional

    public SubscriptionResponse subscribe(Long tenantId, SubscriptionRequest request, Auth currentUser) {

        Tenant tenant = getTenant(tenantId);

       

        // Check permissions: Phải là System Admin hoặc Chủ sở hữu Tenant

        if (currentUser.getSystemRole() != SystemRole.ADMIN &&
            !tenantMembershipService.isTenantOwner(tenantId, currentUser.getId())) {

            throw new UnauthorizedException("No permission to manage billing for this tenant");

        }

       

        Subscription existingSubscription = subscriptionRepository.findByTenant(tenant).orElse(null);

       

        Subscription subscription = existingSubscription != null ?

                updateExistingSubscription(existingSubscription, request) :

                createNewSubscription(tenant, request);

       

        subscriptionRepository.save(subscription);

        processPayment(subscription, request);

       

        return mapToSubscriptionResponse(subscription);

    }



    @Transactional

    public void cancelSubscription(Long tenantId, Auth currentUser) {

        // TODO: Implement cancel logic (thường là set autoRenew = false)

    }



    // --- Invoice Methods ---



    @Transactional(readOnly = true)

    public Page<InvoiceResponse> listInvoices(Long tenantId, Pageable pageable) {

        // TODO: Implement logic truy vấn từ invoiceRepository

        return Page.empty();

    }



    // --- Webhook Handler ---



    public void handleWebhookEvent(String payload, String signature) {

        log.info("Received webhook from payment gateway");

        // TODO: Xử lý sự kiện từ Stripe/Paypal (ví dụ: thanh toán thành công, gia hạn gói)

    }



    // --- Private Helper Methods ---



    private Tenant getTenant(Long tenantId) {

        return tenantRepository.findById(tenantId)

                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

}



// --- Payment Method Methods ---

    @Transactional
    public PaymentMethodResponse addPaymentMethod(Long tenantId, PaymentMethodRequest request, Auth currentUser) {
        // Check permissions
        if (currentUser.getSystemRole() != SystemRole.ADMIN &&
            !tenantMembershipService.isTenantOwner(tenantId, currentUser.getId())) {
            throw new UnauthorizedException("No permission to add payment methods for this tenant");
        }

        // Get the tenant
        Tenant tenant = getTenant(tenantId);
        
        // Check if this is the first payment method for this tenant
        boolean isFirstPaymentMethod = paymentMethodRepository.findByTenant(tenant).isEmpty();
        
        // Create new payment method
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .tenant(tenant)
                .type(request.getType())
                .gatewayId(request.getToken()) // Using token as gatewayId for now
                .isDefault(isFirstPaymentMethod)
                .build();
        
        // Save the payment method
        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
        
        // Return the response
        return mapToPaymentMethodResponse(savedPaymentMethod);
    }
    
    private SubscriptionResponse mapToSubscriptionResponse(Subscription subscription) {
        if (subscription == null) {
            return null;
        }
        
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .plan(subscription.getPlan())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .autoRenew(subscription.isAutoRenew())
                .build();
    }
    
    private PaymentMethodResponse mapToPaymentMethodResponse(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }
        
        return PaymentMethodResponse.builder()
                .id(paymentMethod.getId())
                .type(paymentMethod.getType())
                .lastFour(paymentMethod.getLastFour())
                .isDefault(paymentMethod.isDefault())
                .createdAt(paymentMethod.getCreatedAt())
                .build();
    }
    
    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .amount(invoice.getAmount())
                .currency(invoice.getCurrency())
                // Chuyển Enum sang String, xử lý null để tránh NullPointerException
                .status(invoice.getStatus() != null ? invoice.getStatus().name() : null)
                // Ánh xạ invoiceUrl từ Model vào field invoicePdf của DTO
                .invoicePdf(invoice.getInvoiceUrl()) 
                .receiptUrl(invoice.getReceiptUrl())
                // Ánh xạ issueDate từ Model vào field createdAt của DTO
                .createdAt(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .paidDate(invoice.getPaidDate())
                .description(invoice.getDescription())
                .build();
    }
    private Subscription createNewSubscription(Tenant tenant, SubscriptionRequest request) {

        LocalDateTime now = LocalDateTime.now();

        return Subscription.builder()

                .tenant(tenant)

                .plan(request.getPlan())

                .startDate(now)

                .endDate(now.plusMonths(1))

                .status(SubscriptionStatus.ACTIVE)

                .autoRenew(request.isAutoRenew())

                .build();

    }



    private Subscription updateExistingSubscription(Subscription existing, SubscriptionRequest request) {

        existing.setPlan(request.getPlan());

        existing.setAutoRenew(request.isAutoRenew());

        existing.setUpdatedAt(LocalDateTime.now());

        return existing;

    }



    private void processPayment(Subscription subscription, SubscriptionRequest request) {

        try {

            String paymentIntentId = paymentGatewayService.createPaymentIntent(

                    subscription.getPlan().getPrice(),

                    "usd",

                    "Subscription payment for " + subscription.getTenant().getName()

            );

            log.info("Payment intent created: {}", paymentIntentId);

        } catch (Exception e) {

            log.error("Payment processing failed", e);

            throw new RuntimeException("Failed to process payment: " + e.getMessage());

        }

    }

}