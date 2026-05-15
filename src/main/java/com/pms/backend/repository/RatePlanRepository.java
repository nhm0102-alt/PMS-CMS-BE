package com.pms.backend.repository;

import com.pms.backend.model.RatePlanEntity;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface RatePlanRepository extends SoftDeleteRepository<RatePlanEntity> {
    @Query(value = "SELECT rp.* FROM rate_plans rp " +
                   "JOIN rate_plan_room_types rprt ON rp.id = rprt.rate_plan_id " +
                   "WHERE rprt.room_type_id = ?1 AND rp.deleted = 0", nativeQuery = true)
    List<RatePlanEntity> findByRoomTypeIdAndDeletedFalse(String roomTypeId);

    @Query(value = "SELECT room_type_id FROM rate_plan_room_types WHERE rate_plan_id = ?1", nativeQuery = true)
    List<String> findRoomTypeIdsByRatePlanId(String ratePlanId);

    Optional<RatePlanEntity> findByChannexIdAndDeletedFalse(String channexId);
}
