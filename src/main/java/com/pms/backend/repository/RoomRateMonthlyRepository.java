package com.pms.backend.repository;

import com.pms.backend.model.RoomRateMonthlyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoomRateMonthlyRepository extends JpaRepository<RoomRateMonthlyEntity, Long> {
    List<RoomRateMonthlyEntity> findByPropertyIdAndInYearAndInMonth(String propertyId, Integer inYear, Integer inMonth);
    Optional<RoomRateMonthlyEntity> findByRoomTypeIdAndRatePlanIdAndInYearAndInMonth(String roomTypeId, String ratePlanId, Integer inYear, Integer inMonth);
    Optional<RoomRateMonthlyEntity> findByRatePlanIdAndInYearAndInMonth(String ratePlanId, Integer inYear, Integer inMonth);
}
