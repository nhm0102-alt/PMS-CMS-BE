package com.pms.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoices")
public class InvoiceEntity extends BaseJsonEntity {
}

