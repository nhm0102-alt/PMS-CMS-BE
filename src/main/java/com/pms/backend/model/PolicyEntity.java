package com.pms.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "policies")
public class PolicyEntity extends BaseJsonEntity {
}

