// InvoiceRepository.java

package com.chatbot.modules.tenant.billing.repository;

import com.chatbot.modules.tenant.billing.model.Invoice;
import com.chatbot.modules.tenant.billing.model.Subscription;
import com.chatbot.modules.tenant.core.model.Tenant;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    Optional<Invoice> findByIdAndTenant(String id, Tenant tenant);

    Page<Invoice> findBySubscriptionOrderByIssueDateDesc(Subscription subscription, Pageable pageable);

}

