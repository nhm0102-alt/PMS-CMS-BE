package com.pms.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "rate_plans")
public class RatePlanEntity extends BaseJsonEntity {
    @Column(name = "property_id", length = 36)
    private String propertyId;

    @Column(name = "channex_id", length = 64)
    private String channexId;

    @Column(name = "cancellation_policy_id", length = 36)
    private String cancellationPolicyId;

    @Column(name = "surcharge_policy_id", length = 36)
    private String surchargePolicyId;

    @jakarta.persistence.Transient
    private java.util.List<String> roomTypeIds;

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getChannexId() {
        return channexId;
    }

    public void setChannexId(String channexId) {
        this.channexId = channexId;
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

    public java.util.List<String> getRoomTypeIds() {
        return roomTypeIds;
    }

    public void setRoomTypeIds(java.util.List<String> roomTypeIds) {
        this.roomTypeIds = roomTypeIds;
    }
}

