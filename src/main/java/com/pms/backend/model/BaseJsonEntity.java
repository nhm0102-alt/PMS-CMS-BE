package com.pms.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseJsonEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String dataJson;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false, updatable = false)
    private Instant createdDate;

    @Column(nullable = false)
    private Instant updatedDate;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdDate = now;
        this.updatedDate = now;
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
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
}

