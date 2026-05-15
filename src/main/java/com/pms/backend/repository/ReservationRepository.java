package com.pms.backend.repository;

import com.pms.backend.model.ReservationEntity;
import java.util.Optional;

public interface ReservationRepository extends SoftDeleteRepository<ReservationEntity> {
    Optional<ReservationEntity> findByChannexIdAndDeletedFalse(String channexId);
}
