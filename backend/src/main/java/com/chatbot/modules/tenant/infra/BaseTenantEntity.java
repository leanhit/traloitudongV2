package com.chatbot.modules.tenant.infra;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = Long.class)
)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class BaseTenantEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @PrePersist
    protected void assignTenantId() {
        if (tenantId == null) {
            Long tenantIdFromContext = TenantContext.getTenantId();
            if (tenantIdFromContext == null) {
                throw new IllegalStateException("Tenant ID is missing");
            }
            this.tenantId = tenantIdFromContext;
        }
    }

    public Long getTenantId() {
        return tenantId;
    }
}
