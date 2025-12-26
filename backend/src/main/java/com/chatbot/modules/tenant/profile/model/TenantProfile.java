package com.chatbot.modules.tenant.profile.model;

import com.chatbot.modules.tenant.core.model.Tenant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tenant_profiles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1–1 với Tenant
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;

    // ===== Thông tin pháp lý =====
    private String legalName;
    private String taxCode;
    private String address;

    // ===== Thông tin liên hệ =====
    private String contactEmail;
    private String contactPhone;

    // ===== Branding / White-label =====
    private String logoUrl;
    private String faviconUrl;
    private String primaryColor; // #RRGGBB
}
