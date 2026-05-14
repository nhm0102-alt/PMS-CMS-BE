package com.pms.backend.service;

import com.pms.backend.api.service.RoomAllotmentService;
import com.pms.backend.model.RoomAllotmentMonthlyEntity;
import com.pms.backend.repository.RoomAllotmentMonthlyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
public class RoomAllotmentServiceImpl implements RoomAllotmentService {
    private final RoomAllotmentMonthlyRepository allotmentRepository;

    public RoomAllotmentServiceImpl(RoomAllotmentMonthlyRepository allotmentRepository) {
        this.allotmentRepository = allotmentRepository;
    }

    @Override
    @Transactional
    public void updateAllotmentFromReservation(Map<String, Object> resData, boolean isCancellation) throws Exception {
        String propertyId = (String) resData.get("property_id");
        String roomTypeId = (String) resData.get("room_type_id");
        String checkInStr = (String) resData.get("check_in_date");
        String checkOutStr = (String) resData.get("check_out_date");

        if (propertyId == null || roomTypeId == null || checkInStr == null || checkOutStr == null) return;

        LocalDate checkIn = LocalDate.parse(checkInStr);
        LocalDate checkOut = LocalDate.parse(checkOutStr);

        int adjustment = isCancellation ? 1 : -1;

        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            int year = date.getYear();
            int month = date.getMonthValue();
            int day = date.getDayOfMonth();

            RoomAllotmentMonthlyEntity allotment = allotmentRepository.findByRoomTypeIdAndInYearAndInMonth(roomTypeId, year, month)
                    .orElseGet(() -> {
                        RoomAllotmentMonthlyEntity e = new RoomAllotmentMonthlyEntity();
                        e.setPropertyId(propertyId);
                        e.setRoomTypeId(roomTypeId);
                        e.setInYear(year);
                        e.setInMonth(month);
                        return e;
                    });

            Integer currentVal = allotment.getCol(day);
            if (currentVal == null) currentVal = 0;
            
            allotment.setCol(day, Math.max(0, currentVal + adjustment));
            allotmentRepository.save(allotment);
        }
    }
}
