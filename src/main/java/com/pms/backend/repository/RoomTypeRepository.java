package com.pms.backend.repository;

import com.pms.backend.model.RoomTypeEntity;
import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository extends SoftDeleteRepository<RoomTypeEntity> {
    List<RoomTypeEntity> findByPropertyIdAndDeletedFalse(String propertyId);
    Optional<RoomTypeEntity> findByChannexIdAndDeletedFalse(String channexId);
}
