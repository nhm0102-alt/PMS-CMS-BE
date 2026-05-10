package com.pms.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "guests")
public class GuestEntity extends BaseJsonEntity {
}

