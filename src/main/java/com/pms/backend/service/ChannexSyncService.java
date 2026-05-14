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
    private final ObjectMapper objectMapper;

    public ChannexSyncService(ChannexClient channexClient,
                             PropertyRepository propertyRepository,
                             RoomTypeRepository roomTypeRepository,
                             RatePlanRepository ratePlanRepository,
                             ReservationRepository reservationRepository,
                             RoomAllotmentService roomAllotmentService,
                             RoomAllotmentMonthlyRepository allotmentRepository,
                             RoomRateMonthlyRepository rateRepository,
                             ObjectMapper objectMapper) {
        this.channexClient = channexClient;
        this.propertyRepository = propertyRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.ratePlanRepository = ratePlanRepository;
        this.reservationRepository = reservationRepository;
        this.roomAllotmentService = roomAllotmentService;
        this.allotmentRepository = allotmentRepository;
        this.rateRepository = rateRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 60000) // Every 5 minutes
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
        
        PropertyEntity property = findByChannexId(propertyRepository, channexPropertyId)
                .orElseThrow(() -> new RuntimeException("Property not found for Channex ID: " + channexPropertyId));

        // Find existing reservation by Channex ID to handle updates
        ReservationEntity reservation = findByChannexId(reservationRepository, channexBookingId)
                .orElseGet(ReservationEntity::new);

        Map<String, Object> resData = reservation.getDataJson() != null ? 
                objectMapper.readValue(reservation.getDataJson(), new TypeReference<>() {}) : 
                new HashMap<>();

        // Basic mapping
        resData.put("channex_id", channexBookingId);
        resData.put("property_id", property.getId());
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

            findByChannexId(roomTypeRepository, channexRoomTypeId).ifPresent(rt -> resData.put("room_type_id", rt.getId()));
            findByChannexId(ratePlanRepository, channexRatePlanId).ifPresent(rp -> resData.put("rate_plan_id", rp.getId()));
        }

        reservation.setDataJson(objectMapper.writeValueAsString(resData));
        reservationRepository.save(reservation);

        // Update Allotment
        roomAllotmentService.updateAllotmentFromReservation(resData, "cancelled".equalsIgnoreCase(status));
        
        // Push updated availability to Channex
        String roomTypeId = (String) resData.get("room_type_id");
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

    private <T extends BaseJsonEntity> Optional<T> findByChannexId(SoftDeleteRepository<T> repo, String channexId) {
        if (channexId == null) return Optional.empty();
        return repo.findByDeletedFalse().stream()
                .filter(e -> {
                    try {
                        Map<String, Object> data = objectMapper.readValue(e.getDataJson(), new TypeReference<>() {});
                        return channexId.equals(data.get("channex_id"));
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .findFirst();
    }

    @Transactional
    public void pushAvailability(String roomTypeId, int year, int month) throws Exception {
        RoomTypeEntity roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Room type not found"));
        Map<String, Object> rtData = objectMapper.readValue(roomType.getDataJson(), new TypeReference<>() {});
        String channexRoomTypeId = (String) rtData.get("channex_id");

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
        Map<String, Object> rpData = objectMapper.readValue(ratePlan.getDataJson(), new TypeReference<>() {});
        String channexRatePlanId = (String) rpData.get("channex_id");

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
    public void syncProperty(String propertyId) throws Exception {
        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        Map<String, Object> data = objectMapper.readValue(property.getDataJson(), new TypeReference<>() {});
        if (data.containsKey("channex_id")) return;

        Map<String, Object> channexData = new HashMap<>();
        channexData.put("title", data.get("name"));
        channexData.put("currency", data.getOrDefault("currency", "USD"));
        channexData.put("property_type", "hotel");

        Map<String, Object> response = channexClient.createProperty(channexData);
        Map<String, Object> hotel = (Map<String, Object>) response.get("data");
        String channexId = (String) hotel.get("id");

        data.put("channex_id", channexId);
        property.setDataJson(objectMapper.writeValueAsString(data));
        propertyRepository.save(property);
    }

    @Transactional
    public void syncRoomType(String roomTypeId) throws Exception {
        RoomTypeEntity roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Room type not found"));

        Map<String, Object> data = objectMapper.readValue(roomType.getDataJson(), new TypeReference<>() {});
        if (data.containsKey("channex_id")) return;

        String propertyId = (String) data.get("property_id");
        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        Map<String, Object> propData = objectMapper.readValue(property.getDataJson(), new TypeReference<>() {});
        String channexPropertyId = (String) propData.get("channex_id");

        if (channexPropertyId == null) throw new RuntimeException("Property must be synced to Channex first");

        Map<String, Object> channexData = new HashMap<>();
        channexData.put("title", data.get("name"));
        channexData.put("property_id", channexPropertyId);
        channexData.put("occ_base", data.getOrDefault("base_occupancy", 2));
        channexData.put("occ_max", data.getOrDefault("max_occupancy", 2));

        Map<String, Object> response = channexClient.createRoomType(channexData);
        Map<String, Object> rt = (Map<String, Object>) response.get("data");
        String channexId = (String) rt.get("id");

        data.put("channex_id", channexId);
        roomType.setDataJson(objectMapper.writeValueAsString(data));
        roomTypeRepository.save(roomType);
    }

    @Transactional
    public void syncRatePlan(String ratePlanId) throws Exception {
        RatePlanEntity ratePlan = ratePlanRepository.findById(ratePlanId)
                .orElseThrow(() -> new RuntimeException("Rate plan not found"));

        Map<String, Object> data = objectMapper.readValue(ratePlan.getDataJson(), new TypeReference<>() {});
        if (data.containsKey("channex_id")) return;

        String roomTypeId = (String) data.get("room_type_id");
        RoomTypeEntity roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Room type not found"));
        Map<String, Object> rtData = objectMapper.readValue(roomType.getDataJson(), new TypeReference<>() {});
        String channexRoomTypeId = (String) rtData.get("channex_id");

        if (channexRoomTypeId == null) throw new RuntimeException("Room type must be synced to Channex first");

        Map<String, Object> channexData = new HashMap<>();
        channexData.put("title", data.get("name"));
        channexData.put("room_type_id", channexRoomTypeId);
        channexData.put("currency", rtData.getOrDefault("currency", "USD"));

        Map<String, Object> response = channexClient.createRatePlan(channexData);
        Map<String, Object> rp = (Map<String, Object>) response.get("data");
        String channexId = (String) rp.get("id");

        data.put("channex_id", channexId);
        ratePlan.setDataJson(objectMapper.writeValueAsString(data));
        ratePlanRepository.save(ratePlan);
    }
}
