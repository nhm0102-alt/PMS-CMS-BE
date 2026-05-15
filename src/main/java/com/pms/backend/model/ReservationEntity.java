package com.pms.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class ReservationEntity extends BaseJsonEntity {
    @Column(name = "property_id", length = 36)
    private String propertyId;

    @Column(name = "room_type_id", length = 36)
    private String roomTypeId;

    @Column(name = "rate_plan_id", length = 36)
    private String ratePlanId;

    @Column(name = "room_id", length = 36)
    private String roomId;

    @Column(name = "guest_id", length = 36)
    private String guestId;

    @Column(name = "channex_id", length = 64)
    private String channexId;

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(String roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getRatePlanId() {
        return ratePlanId;
    }

    public void setRatePlanId(String ratePlanId) {
        this.ratePlanId = ratePlanId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getChannexId() {
        return channexId;
    }

    public void setChannexId(String channexId) {
        this.channexId = channexId;
    }
}

