package com.pms.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class ReservationEntity extends BaseJsonEntity {
}

