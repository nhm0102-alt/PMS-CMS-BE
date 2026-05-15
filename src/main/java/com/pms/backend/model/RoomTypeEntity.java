package com.pms.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "room_types")
public class RoomTypeEntity extends BaseJsonEntity {
    @Column(name = "property_id", length = 36)
    private String propertyId;

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }
}

