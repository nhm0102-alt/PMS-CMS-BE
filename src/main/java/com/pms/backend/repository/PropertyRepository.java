package com.pms.backend.repository;

import com.pms.backend.model.PropertyEntity;
import java.util.Optional;

public interface PropertyRepository extends SoftDeleteRepository<PropertyEntity> {
    Optional<PropertyEntity> findByChannexIdAndDeletedFalse(String channexId);
}
