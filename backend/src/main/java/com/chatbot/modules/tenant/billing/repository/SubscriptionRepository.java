// SubscriptionRepository.java

package com.chatbot.modules.tenant.billing.repository;

import com.chatbot.modules.tenant.billing.model.Subscription;

import com.chatbot.modules.tenant.billing.model.SubscriptionStatus;

import com.chatbot.modules.tenant.core.model.Tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByTenant(Tenant tenant);

    List<Subscription> findByStatus(SubscriptionStatus status);

   

    @Query("SELECT s FROM Subscription s WHERE s.endDate < CURRENT_DATE AND s.status = 'ACTIVE'")

    List<Subscription> findExpiredSubscriptions();

}