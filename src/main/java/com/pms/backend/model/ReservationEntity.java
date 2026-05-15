package com.pms.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class ReservationEntity extends BaseJsonEntity {
    @Column(name = "channex_id", length = 64)
    private String channexId;

    public String getChannexId() {
        return channexId;
    }

    public void setChannexId(String channexId) {
        this.channexId = channexId;
    }
}

