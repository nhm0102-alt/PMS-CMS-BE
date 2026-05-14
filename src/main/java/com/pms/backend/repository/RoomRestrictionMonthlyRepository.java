package com.pms.backend.repository;

import com.pms.backend.model.RoomRestrictionMonthlyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoomRestrictionMonthlyRepository extends JpaRepository<RoomRestrictionMonthlyEntity, Long> {
    List<RoomRestrictionMonthlyEntity> findByPropertyIdAndInYearAndInMonth(String propertyId, Integer inYear, Integer inMonth);
    Optional<RoomRestrictionMonthlyEntity> findByRoomTypeIdAndRatePlanIdAndRestrictionTypeAndInYearAndInMonth(String roomTypeId, String ratePlanId, String restrictionType, Integer inYear, Integer inMonth);
}
