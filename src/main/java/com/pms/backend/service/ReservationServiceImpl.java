package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.ReservationService;
import com.pms.backend.api.service.RoomAllotmentService;
import com.pms.backend.model.ReservationEntity;
import com.pms.backend.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ReservationServiceImpl extends AbstractRestEntityServiceImpl<ReservationEntity> implements ReservationService {
    private final ChannexSyncService channexSyncService;
    private final RoomAllotmentService roomAllotmentService;

    public ReservationServiceImpl(ReservationRepository repository, ObjectMapper objectMapper, ChannexSyncService channexSyncService, RoomAllotmentService roomAllotmentService) {
        super(repository, objectMapper, ReservationEntity::new);
        this.channexSyncService = channexSyncService;
        this.roomAllotmentService = roomAllotmentService;
    }

    @Override
    protected void syncEntityWithData(ReservationEntity entity, Map<String, Object> data) {
        if (data != null) {
            entity.setChannexId((String) data.get("channex_id"));
        }
    }

    @Override
    protected void afterSave(ReservationEntity entity, Map<String, Object> data) {
        try {
            // Only update allotment for new reservations to avoid double counting on updates
            if (entity.getCreatedDate().equals(entity.getUpdatedDate())) {
                roomAllotmentService.updateAllotmentFromReservation(data, false);
                
                // Push updated availability to Channex
                String roomTypeId = (String) data.get("room_type_id");
                if (roomTypeId != null) {
                    LocalDate checkIn = LocalDate.parse((String) data.get("check_in_date"));
                    LocalDate checkOut = LocalDate.parse((String) data.get("check_out_date"));
                    Set<String> months = new HashSet<>();
                    for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                        months.add(date.getYear() + "-" + date.getMonthValue());
                    }
                    for (String m : months) {
                        String[] parts = m.split("-");
                        channexSyncService.pushAvailability(roomTypeId, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void normalize(Map<String, Object> reservation) {
        String checkIn = reservation.get("check_in_date") == null ? null : reservation.get("check_in_date").toString();
        String checkOut = reservation.get("check_out_date") == null ? null : reservation.get("check_out_date").toString();
        Object nightsObj = reservation.get("nights");
        Integer nights = null;
        if (nightsObj instanceof Number n) {
            nights = n.intValue();
        } else if (nightsObj != null) {
            try {
                nights = Integer.parseInt(nightsObj.toString());
            } catch (Exception ex) {
                nights = null;
            }
        }
        if ((nights == null || nights <= 0) && checkIn != null && checkOut != null) {
            try {
                LocalDate ci = LocalDate.parse(checkIn);
                LocalDate co = LocalDate.parse(checkOut);
                long diff = ChronoUnit.DAYS.between(ci, co);
                if (diff > 0) {
                    reservation.put("nights", (int) diff);
                }
            } catch (Exception ex) {
            }
        }

        BigDecimal total = decimalOrZero(reservation.get("total_amount"));
        BigDecimal paid = decimalOrZero(reservation.get("paid_amount"));
        BigDecimal deposit = decimalOrZero(reservation.get("deposit_amount"));
        BigDecimal balance = total.subtract(paid).subtract(deposit);
        if (balance.signum() < 0) {
            balance = BigDecimal.ZERO;
        }

        reservation.put("total_amount", total);
        reservation.put("paid_amount", paid);
        reservation.put("deposit_amount", deposit);
        reservation.put("balance_due", balance);
    }

    private static BigDecimal decimalOrNull(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        String s = v.toString();
        if (s.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(s);
        } catch (Exception ex) {
            return null;
        }
    }

    private static BigDecimal decimalOrZero(Object v) {
        BigDecimal d = decimalOrNull(v);
        return d == null ? BigDecimal.ZERO : d;
    }
}
