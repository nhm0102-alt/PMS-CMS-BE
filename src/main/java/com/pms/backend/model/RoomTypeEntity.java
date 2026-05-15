package com.pms.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "room_types")
public class RoomTypeEntity extends BaseJsonEntity {
    @Column(name = "property_id", length = 36)
    private String propertyId;

    @Column(name = "channex_id", length = 64)
    private String channexId;

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
}

