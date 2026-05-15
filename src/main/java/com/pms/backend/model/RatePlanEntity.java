package com.pms.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "rate_plans")
public class RatePlanEntity extends BaseJsonEntity {
    @Column(name = "property_id", length = 36)
    private String propertyId;

    @Column(name = "cancellation_policy_id", length = 36)
    private String cancellationPolicyId;

    @Column(name = "surcharge_policy_id", length = 36)
    private String surchargePolicyId;

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getCancellationPolicyId() {
        return cancellationPolicyId;
    }

    public void setCancellationPolicyId(String cancellationPolicyId) {
        this.cancellationPolicyId = cancellationPolicyId;
    }

    public String getSurchargePolicyId() {
        return surchargePolicyId;
    }

    public void setSurchargePolicyId(String surchargePolicyId) {
        this.surchargePolicyId = surchargePolicyId;
    }
}

