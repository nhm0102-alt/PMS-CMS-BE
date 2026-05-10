package com.pms.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "entity_records")
public class EntityRecord {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 64)
    private String entityName;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String dataJson;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false, updatable = false)
    private Instant createdDate;

    @Column(nullable = false)
    private Instant updatedDate;

    @Column
    private String createdBy;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdDate = now;
        this.updatedDate = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedDate = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
