package com.pms.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_properties")
public class UserPropertyEntity extends BaseJsonEntity {
}

