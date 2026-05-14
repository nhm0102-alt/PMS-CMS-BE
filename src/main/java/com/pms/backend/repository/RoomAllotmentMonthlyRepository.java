package com.pms.backend.repository;

import com.pms.backend.model.RoomAllotmentMonthlyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoomAllotmentMonthlyRepository extends JpaRepository<RoomAllotmentMonthlyEntity, Long> {
    List<RoomAllotmentMonthlyEntity> findByPropertyIdAndInYearAndInMonth(String propertyId, Integer inYear, Integer inMonth);
    Optional<RoomAllotmentMonthlyEntity> findByRoomTypeIdAndInYearAndInMonth(String roomTypeId, Integer inYear, Integer inMonth);
}
