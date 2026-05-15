package com.pms.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.model.*;
import com.pms.backend.api.service.RoomAllotmentService;
import com.pms.backend.repository.*;
import com.pms.backend.model.ReservationEntity;
import com.pms.backend.model.BaseJsonEntity;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ChannexSyncService {
    private final ChannexClient channexClient;
    private final PropertyRepository propertyRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RatePlanRepository ratePlanRepository;
    private final ReservationRepository reservationRepository;
    private final RoomAllotmentService roomAllotmentService;
    private final RoomAllotmentMonthlyRepository allotmentRepository;
    private final RoomRateMonthlyRepository rateRepository;
    private final RoomRestrictionMonthlyRepository restrictionRepository;
    private final ObjectMapper objectMapper;

    public ChannexSyncService(ChannexClient channexClient,
                             PropertyRepository propertyRepository,
                             RoomTypeRepository roomTypeRepository,
                             RatePlanRepository ratePlanRepository,
                             ReservationRepository reservationRepository,
                             RoomAllotmentService roomAllotmentService,
                             RoomAllotmentMonthlyRepository allotmentRepository,
                             RoomRateMonthlyRepository rateRepository,
                             RoomRestrictionMonthlyRepository restrictionRepository,
                             ObjectMapper objectMapper) {
        this.channexClient = channexClient;
        this.propertyRepository = propertyRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.ratePlanRepository = ratePlanRepository;
        this.reservationRepository = reservationRepository;
        this.roomAllotmentService = roomAllotmentService;
        this.allotmentRepository = allotmentRepository;
        this.rateRepository = rateRepository;
        this.restrictionRepository = restrictionRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 60000) // Every 1 minute
    public void scheduledPullBookings() {
        try {
            pullBookings();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void pullBookings() throws Exception {
        Map<String, Object> response = channexClient.getBookingRevisions();
        if (response == null || !response.containsKey("data")) return;
        
        List<Map<String, Object>> revisions = (List<Map<String, Object>>) response.get("data");
        for (Map<String, Object> revision : revisions) {
            String revisionId = (String) revision.get("id");
            Map<String, Object> booking = (Map<String, Object>) revision.get("booking");
            
            try {
                processBooking(booking);
                channexClient.acknowledgeBookingRevision(revisionId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processBooking(Map<String, Object> channexBooking) throws Exception {
        String channexPropertyId = (String) channexBooking.get("property_id");
        String channexBookingId = (String) channexBooking.get("id");
        String status = (String) channexBooking.get("status");
        
        PropertyEntity property = propertyRepository.findByChannexIdAndDeletedFalse(channexPropertyId)
                .orElseThrow(() -> new RuntimeException("Property not found for Channex ID: " + channexPropertyId));

        // Find existing reservation by Channex ID to handle updates
        ReservationEntity reservation = reservationRepository.findByChannexIdAndDeletedFalse(channexBookingId)
                .orElseGet(ReservationEntity::new);

        Map<String, Object> resData = reservation.getDataJson() != null ? 
                objectMapper.readValue(reservation.getDataJson(), new TypeReference<>() {}) : 
                new HashMap<>();

        // Basic mapping
        resData.put("status", status);
        resData.put("check_in_date", channexBooking.get("arrival_date"));
        resData.put("check_out_date", channexBooking.get("departure_date"));
        resData.put("guest_name", channexBooking.get("customer_name"));
        resData.put("total_amount", channexBooking.get("amount"));
        resData.put("currency", channexBooking.get("currency"));
        resData.put("source", "Channex");

        // Map room types and rate plans
        List<Map<String, Object>> rooms = (List<Map<String, Object>>) channexBooking.get("booking_rooms");
        if (rooms != null && !rooms.isEmpty()) {
            Map<String, Object> firstRoom = rooms.get(0);
            String channexRoomTypeId = (String) firstRoom.get("room_type_id");
            String channexRatePlanId = (String) firstRoom.get("rate_plan_id");

            roomTypeRepository.findByChannexIdAndDeletedFalse(channexRoomTypeId).ifPresent(rt -> {
                reservation.setRoomTypeId(rt.getId());
            });
            ratePlanRepository.findByChannexIdAndDeletedFalse(channexRatePlanId).ifPresent(rp -> {
                reservation.setRatePlanId(rp.getId());
            });
        }

        reservation.setChannexId(channexBookingId);
        reservation.setPropertyId(property.getId());
        reservation.setDataJson(objectMapper.writeValueAsString(resData));
        reservationRepository.save(reservation);

        // Update Allotment
        roomAllotmentService.updateAllotmentFromReservation(resData, "cancelled".equalsIgnoreCase(status));
        
        // Push updated availability to Channex
        String roomTypeId = reservation.getRoomTypeId();
        if (roomTypeId != null) {
            LocalDate checkIn = LocalDate.parse((String) resData.get("check_in_date"));
            LocalDate checkOut = LocalDate.parse((String) resData.get("check_out_date"));
            Set<String> months = new HashSet<>();
            for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                months.add(date.getYear() + "-" + date.getMonthValue());
            }
            for (String m : months) {
                String[] parts = m.split("-");
                pushAvailability(roomTypeId, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
        }
    }

    @Transactional
    public void pushAvailability(String roomTypeId, int year, int month) throws Exception {
        RoomTypeEntity roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Room type not found"));
        String channexRoomTypeId = roomType.getChannexId();

        if (channexRoomTypeId == null) return;

        RoomAllotmentMonthlyEntity allotment = allotmentRepository.findByRoomTypeIdAndInYearAndInMonth(roomTypeId, year, month)
                .orElse(null);
        if (allotment == null) return;

        List<Map<String, Object>> updates = new ArrayList<>();
        for (int day = 1; day <= 31; day++) {
            Integer value = allotment.getCol(day);
            if (value != null) {
                Map<String, Object> update = new HashMap<>();
                update.put("room_type_id", channexRoomTypeId);
                update.put("date", String.format("%04d-%02d-%02d", year, month, day));
                update.put("availability", value);
                updates.add(update);
            }
        }

        if (!updates.isEmpty()) {
            channexClient.pushARI(Map.of("values", updates));
        }
    }

    @Transactional
    public void pushRates(String ratePlanId, int year, int month) throws Exception {
        RatePlanEntity ratePlan = ratePlanRepository.findById(ratePlanId)
                .orElseThrow(() -> new RuntimeException("Rate plan not found"));
        String channexRatePlanId = ratePlan.getChannexId();

        if (channexRatePlanId == null) return;

        RoomRateMonthlyEntity rate = rateRepository.findByRatePlanIdAndInYearAndInMonth(ratePlanId, year, month)
                .orElse(null);
        if (rate == null) return;

        List<Map<String, Object>> updates = new ArrayList<>();
        for (int day = 1; day <= 31; day++) {
            java.math.BigDecimal value = rate.getCol(day);
            if (value != null) {
                Map<String, Object> update = new HashMap<>();
                update.put("rate_plan_id", channexRatePlanId);
                update.put("date", String.format("%04d-%02d-%02d", year, month, day));
                update.put("amount", value);
                updates.add(update);
            }
        }

        if (!updates.isEmpty()) {
            channexClient.pushARI(Map.of("values", updates));
        }
    }

    @Transactional
    public void pushRestrictions(String roomTypeId, String ratePlanId, int year, int month) throws Exception {
        RatePlanEntity ratePlan = ratePlanRepository.findById(ratePlanId)
                .orElseThrow(() -> new RuntimeException("Rate plan not found"));
        String channexRatePlanId = ratePlan.getChannexId();

        if (channexRatePlanId == null) return;

        List<RoomRestrictionMonthlyEntity> restrictions = restrictionRepository.findByPropertyIdAndInYearAndInMonth(
                ratePlan.getPropertyId(), year, month);

        List<Map<String, Object>> updates = new ArrayList<>();
        
        // Group restrictions by date for this specific rate plan
        Map<String, Map<String, Object>> dateUpdates = new HashMap<>();

        for (RoomRestrictionMonthlyEntity r : restrictions) {
            if (!ratePlanId.equals(r.getRatePlanId())) continue;

            for (int day = 1; day <= 31; day++) {
                String value = r.getCol(day);
                if (value != null) {
                    String dateStr = String.format("%04d-%02d-%02d", year, month, day);
                    Map<String, Object> update = dateUpdates.computeIfAbsent(dateStr, k -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("rate_plan_id", channexRatePlanId);
                        m.put("date", dateStr);
                        return m;
                    });

                    switch (r.getRestrictionType()) {
                        case "is_closed":
                            update.put("stop_sales", "1".equals(value) || "true".equalsIgnoreCase(value) ? 1 : 0);
                            break;
                        case "min_night":
                            update.put("min_stay_arrival", Integer.parseInt(value));
                            break;
                        case "max_night":
                            update.put("max_stay_arrival", Integer.parseInt(value));
                            break;
                        case "cutoff":
                            update.put("min_notice_period", Integer.parseInt(value));
                            break;
                    }
                }
            }
        }

        if (!dateUpdates.isEmpty()) {
            channexClient.pushARI(Map.of("values", new ArrayList<>(dateUpdates.values())));
        }
    }

    @Transactional
    public void syncAllData() throws Exception {
        List<PropertyEntity> properties = propertyRepository.findByDeletedFalse();
        for (PropertyEntity property : properties) {
            syncProperty(property.getId());
            
            String propertyId = property.getId();

            List<RoomTypeEntity> roomTypes = roomTypeRepository.findByPropertyIdAndDeletedFalse(propertyId);

            for (RoomTypeEntity rt : roomTypes) {
                syncRoomType(rt.getId());
                
                String rtId = rt.getId();
                List<RatePlanEntity> ratePlans = ratePlanRepository.findByRoomTypeIdAndDeletedFalse(rtId);

                for (RatePlanEntity rp : ratePlans) {
                    syncRatePlan(rp.getId());
                    
                    // Push ARI for next 3 months
                    LocalDate now = LocalDate.now();
                    for (int i = 0; i < 3; i++) {
                        LocalDate d = now.plusMonths(i);
                        int year = d.getYear();
                        int month = d.getMonthValue();
                        
                        pushAvailability(rt.getId(), year, month);
                        pushRates(rp.getId(), year, month);
                        pushRestrictions(rt.getId(), rp.getId(), year, month);
                    }
                }
            }
        }
    }

    @Transactional
    public void syncProperty(String propertyId) throws Exception {
        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (property.getChannexId() != null) return;

        Map<String, Object> data = objectMapper.readValue(property.getDataJson(), new TypeReference<>() {});

        Map<String, Object> channexData = new HashMap<>();
        channexData.put("title", data.get("name"));
        channexData.put("currency", data.getOrDefault("currency", "USD"));
        channexData.put("property_type", "hotel");

        Map<String, Object> response = channexClient.createProperty(channexData);
        Map<String, Object> hotel = (Map<String, Object>) response.get("data");
        String channexId = (String) hotel.get("id");

        property.setChannexId(channexId);
        property.setDataJson(objectMapper.writeValueAsString(data));
        propertyRepository.save(property);
    }

    @Transactional
    public void syncRoomType(String roomTypeId) throws Exception {
        RoomTypeEntity roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Room type not found"));

        if (roomType.getChannexId() != null) return;

        Map<String, Object> data = objectMapper.readValue(roomType.getDataJson(), new TypeReference<>() {});

        String propertyId = roomType.getPropertyId();
        if (propertyId == null) {
            propertyId = (String) data.get("property_id");
        }
        
        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        String channexPropertyId = property.getChannexId();

        if (channexPropertyId == null) throw new RuntimeException("Property must be synced to Channex first");

        Map<String, Object> channexData = new HashMap<>();
        channexData.put("title", data.get("name"));
        channexData.put("property_id", channexPropertyId);
        channexData.put("description", data.get("description"));
        channexData.put("count_of_rooms", data.getOrDefault("total_rooms", 1));
        channexData.put("occupancy", data.getOrDefault("standard_adults", 2));
        channexData.put("occ_adults", data.getOrDefault("max_adults", 2));
        channexData.put("occ_children", data.getOrDefault("max_children", 0));
        channexData.put("occ_infants", data.getOrDefault("max_infants", 0));

        Map<String, Object> response = channexClient.createRoomType(channexData);
        Map<String, Object> rt = (Map<String, Object>) response.get("data");
        String channexId = (String) rt.get("id");

        roomType.setChannexId(channexId);
        roomType.setDataJson(objectMapper.writeValueAsString(data));
        roomType.setPropertyId(propertyId);
        roomTypeRepository.save(roomType);
    }

    @Transactional
    public void syncRatePlan(String ratePlanId) throws Exception {
        RatePlanEntity ratePlan = ratePlanRepository.findById(ratePlanId)
                .orElseThrow(() -> new RuntimeException("Rate plan not found"));

        if (ratePlan.getChannexId() != null) return;

        List<String> roomTypeIds = ratePlanRepository.findRoomTypeIdsByRatePlanId(ratePlanId);
        if (roomTypeIds == null || roomTypeIds.isEmpty()) return;

        Map<String, Object> data = objectMapper.readValue(ratePlan.getDataJson(), new TypeReference<>() {});

        // Channex Rate Plan is usually linked to one Room Type, but our PMS supports many.
        // For sync, we might need to create multiple rate plans on Channex or pick one.
        // Assuming we pick the first one for simplicity in this demo sync logic.
        String roomTypeId = roomTypeIds.get(0);
        
        RoomTypeEntity roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Room type not found"));
        String channexRoomTypeId = roomType.getChannexId();

        if (channexRoomTypeId == null) throw new RuntimeException("Room type must be synced to Channex first");

        String propertyId = ratePlan.getPropertyId();
        if (propertyId == null) {
            propertyId = roomType.getPropertyId();
        }


        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        String channexPropertyId = property.getChannexId();

        if (channexPropertyId == null) throw new RuntimeException("Property must be synced to Channex first");

        Map<String, Object> channexData = new HashMap<>();
        channexData.put("title", data.get("name"));
        channexData.put("property_id", channexPropertyId);
        channexData.put("room_type_id", channexRoomTypeId);
        channexData.put("currency", data.getOrDefault("currency", "USD"));
        
        // Add required options
        int maxOcc = (int) data.getOrDefault("max_occupancy", 2);
        List<Map<String, Object>> options = new ArrayList<>();
        Map<String, Object> primaryOption = new HashMap<>();
        primaryOption.put("occupancy", maxOcc);
        primaryOption.put("is_primary", true);
        primaryOption.put("rate", 0);
        options.add(primaryOption);
        channexData.put("options", options);

        Map<String, Object> response = channexClient.createRatePlan(channexData);
        Map<String, Object> rp = (Map<String, Object>) response.get("data");
        String channexId = (String) rp.get("id");

        ratePlan.setChannexId(channexId);
        ratePlan.setDataJson(objectMapper.writeValueAsString(data));
        ratePlan.setPropertyId(propertyId);
        ratePlanRepository.save(ratePlan);
    }
}
