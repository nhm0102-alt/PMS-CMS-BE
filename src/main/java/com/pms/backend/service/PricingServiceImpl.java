package com.pms.backend.service;

import com.pms.backend.api.service.PricingService;
import com.pms.backend.model.RoomAllotmentMonthlyEntity;
import com.pms.backend.model.RoomRateMonthlyEntity;
import com.pms.backend.model.RoomRestrictionMonthlyEntity;
import com.pms.backend.repository.RoomAllotmentMonthlyRepository;
import com.pms.backend.repository.RoomRateMonthlyRepository;
import com.pms.backend.repository.RoomRestrictionMonthlyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PricingServiceImpl implements PricingService {

    private final RoomRateMonthlyRepository rateRepository;
    private final RoomAllotmentMonthlyRepository allotmentRepository;
    private final RoomRestrictionMonthlyRepository restrictionRepository;
    private final ChannexSyncService channexSyncService;

    public PricingServiceImpl(RoomRateMonthlyRepository rateRepository,
                              RoomAllotmentMonthlyRepository allotmentRepository,
                              RoomRestrictionMonthlyRepository restrictionRepository,
                              ChannexSyncService channexSyncService) {
        this.rateRepository = rateRepository;
        this.allotmentRepository = allotmentRepository;
        this.restrictionRepository = restrictionRepository;
        this.channexSyncService = channexSyncService;
    }

    @Override
    public Map<String, Object> getInventory(String propertyId, String startDateStr, int days) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = startDate.plusDays(days - 1);

        Map<String, Object> result = new HashMap<>();

        // Collect all months involved
        Set<String> months = new HashSet<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            months.add(d.getYear() + "-" + d.getMonthValue());
        }

        for (String monthStr : months) {
            String[] parts = monthStr.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            List<RoomRateMonthlyEntity> rates = rateRepository.findByPropertyIdAndInYearAndInMonth(propertyId, year, month);
            List<RoomAllotmentMonthlyEntity> allotments = allotmentRepository.findByPropertyIdAndInYearAndInMonth(propertyId, year, month);
            List<RoomRestrictionMonthlyEntity> restrictions = restrictionRepository.findByPropertyIdAndInYearAndInMonth(propertyId, year, month);

            // Pre-process allotments for quick lookup
            Map<String, Integer> allotmentMap = new HashMap<>(); // rtId|dateStr -> value
            for (RoomAllotmentMonthlyEntity a : allotments) {
                for (int day = 1; day <= 31; day++) {
                    Integer val = a.getCol(day);
                    if (val != null) {
                        try {
                            LocalDate d = LocalDate.of(year, month, day);
                            allotmentMap.put(a.getRoomTypeId() + "|" + d.toString(), val);
                        } catch (Exception e) {}
                    }
                }
            }

            // Process rates
            for (RoomRateMonthlyEntity rate : rates) {
                processMonthlyEntity(result, rate.getRoomTypeId(), rate.getRatePlanId(), year, month, startDate, endDate, "price", day -> rate.getCol(day));
            }

            // Process restrictions
            for (RoomRestrictionMonthlyEntity restriction : restrictions) {
                processMonthlyEntity(result, restriction.getRoomTypeId(), restriction.getRatePlanId(), year, month, startDate, endDate, restriction.getRestrictionType(), day -> restriction.getCol(day));
            }

            // Inject allotments into all cells for the room type
            for (int i = 0; i < days; i++) {
                LocalDate d = startDate.plusDays(i);
                String ds = d.toString();
                for (String key : new HashSet<>(result.keySet())) {
                    if (key.endsWith("|" + ds)) {
                        String rtId = key.split("\\|")[0];
                        Integer allot = allotmentMap.get(rtId + "|" + ds);
                        if (allot != null) {
                            Map<String, Object> cell = (Map<String, Object>) result.get(key);
                            cell.put("allotment", allot);
                        }
                    }
                }
            }
        }

        return result;
    }

    private void processMonthlyEntity(Map<String, Object> result, String rtId, String rpId, int year, int month, LocalDate start, LocalDate end, String field, java.util.function.Function<Integer, Object> colGetter) {
        for (int day = 1; day <= 31; day++) {
            try {
                LocalDate date = LocalDate.of(year, month, day);
                if ((date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end))) {
                    String dateStr = date.toString();
                    Object value = colGetter.apply(day);
                    if (value != null) {
                        // If rpId is null, it's a room-type level property (like allotment)
                        // We'll use a placeholder or handle it specifically.
                        // For PricingCalendar FE, it expects rtId|rpId|dateStr.
                        // We might need to handle this in a way that the FE understands.
                        String keyPrefix = rtId + "|" + (rpId != null ? rpId : "*");
                        String key = keyPrefix + "|" + dateStr;
                        
                        Map<String, Object> cell = (Map<String, Object>) result.computeIfAbsent(key, k -> new HashMap<String, Object>());
                        
                        // Special handling for boolean-like restrictions
                        if ("is_closed".equals(field)) {
                            cell.put("closed", "1".equals(value.toString()) || "true".equalsIgnoreCase(value.toString()));
                        } else if ("min_night".equals(field)) {
                            cell.put("min_night", Integer.parseInt(value.toString()));
                        } else if ("max_night".equals(field)) {
                            cell.put("max_night", Integer.parseInt(value.toString()));
                        } else if ("cutoff".equals(field)) {
                            cell.put("cutoff", Integer.parseInt(value.toString()));
                        } else {
                            cell.put(field, value);
                        }
                    }
                }
            } catch (Exception e) {
                // Invalid date (e.g., Feb 30), skip
            }
        }
    }

    @Override
    @Transactional
    public void updateInventory(String propertyId, Map<String, Object> request) {
        String rtId = (String) request.get("roomTypeId");
        String rpId = (String) request.get("ratePlanId");
        String dateStr = (String) request.get("dateStr");
        LocalDate date = LocalDate.parse(dateStr);
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        // Update Price
        if (request.containsKey("price")) {
            BigDecimal price = request.get("price") != null ? new BigDecimal(request.get("price").toString()) : null;
            RoomRateMonthlyEntity rate = rateRepository.findByRoomTypeIdAndRatePlanIdAndInYearAndInMonth(rtId, rpId, year, month)
                    .orElseGet(() -> {
                        RoomRateMonthlyEntity e = new RoomRateMonthlyEntity();
                        e.setPropertyId(propertyId);
                        e.setRoomTypeId(rtId);
                        e.setRatePlanId(rpId);
                        e.setInYear(year);
                        e.setInMonth(month);
                        return e;
                    });
            rate.setCol(day, price);
            rateRepository.save(rate);

            try {
                channexSyncService.pushRates(rpId, year, month);
            } catch (Exception e) {
                // Log error but don't fail the local update
                e.printStackTrace();
            }
        }

        // Update Allotment
        if (request.containsKey("allotment")) {
            Integer allotment = request.get("allotment") != null ? Integer.parseInt(request.get("allotment").toString()) : null;
            RoomAllotmentMonthlyEntity a = allotmentRepository.findByRoomTypeIdAndInYearAndInMonth(rtId, year, month)
                    .orElseGet(() -> {
                        RoomAllotmentMonthlyEntity e = new RoomAllotmentMonthlyEntity();
                        e.setPropertyId(propertyId);
                        e.setRoomTypeId(rtId);
                        e.setInYear(year);
                        e.setInMonth(month);
                        return e;
                    });
            a.setCol(day, allotment);
            allotmentRepository.save(a);

            try {
                channexSyncService.pushAvailability(rtId, year, month);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Update Restrictions
        updateRestriction(propertyId, rtId, rpId, year, month, day, "is_closed", request.get("closed"));
        updateRestriction(propertyId, rtId, rpId, year, month, day, "min_night", request.get("min_night"));
        updateRestriction(propertyId, rtId, rpId, year, month, day, "max_night", request.get("max_night"));
        updateRestriction(propertyId, rtId, rpId, year, month, day, "cutoff", request.get("cutoff"));

        try {
            channexSyncService.pushRestrictions(rtId, rpId, year, month);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateRestriction(String propertyId, String rtId, String rpId, int year, int month, int day, String type, Object value) {
        if (value == null) return;
        String valStr = value.toString();
        if (value instanceof Boolean) {
            valStr = (Boolean) value ? "1" : "0";
        }

        RoomRestrictionMonthlyEntity r = restrictionRepository.findByRoomTypeIdAndRatePlanIdAndRestrictionTypeAndInYearAndInMonth(rtId, rpId, type, year, month)
                .orElseGet(() -> {
                    RoomRestrictionMonthlyEntity e = new RoomRestrictionMonthlyEntity();
                    e.setPropertyId(propertyId);
                    e.setRoomTypeId(rtId);
                    e.setRatePlanId(rpId);
                    e.setRestrictionType(type);
                    e.setInYear(year);
                    e.setInMonth(month);
                    return e;
                });
        r.setCol(day, valStr);
        restrictionRepository.save(r);
    }
}
