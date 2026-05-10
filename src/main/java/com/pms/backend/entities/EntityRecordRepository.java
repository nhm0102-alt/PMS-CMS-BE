package com.pms.backend.entities;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntityRecordRepository extends JpaRepository<EntityRecord, String> {
    List<EntityRecord> findByEntityNameAndDeletedFalse(String entityName);

    Optional<EntityRecord> findByEntityNameAndId(String entityName, String id);
}
