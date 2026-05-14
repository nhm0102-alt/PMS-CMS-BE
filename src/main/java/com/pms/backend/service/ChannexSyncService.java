package com.pms.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.model.PropertyEntity;
import com.pms.backend.model.RatePlanEntity;
import com.pms.backend.model.RoomTypeEntity;
import com.pms.backend.repository.PropertyRepository;
import com.pms.backend.repository.RatePlanRepository;
import com.pms.backend.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import com.pms.backend.repository.RoomAllotmentMonthlyRepository;
import com.pms.backend.repository.RoomRateMonthlyRepository;
import com.pms.backend.model.RoomAllotmentMonthlyEntity;
import com.pms.backend.model.RoomRateMonthlyEntity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChannexSyncService {
    private final ChannexClient channexClient;
    private final PropertyRepository propertyRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RatePlanRepository ratePlanRepository;
    private final RoomAllotmentMonthlyRepository allotmentRepository;
    private final RoomRateMonthlyRepository rateRepository;
    private final ObjectMapper objectMapper;

    public ChannexSyncService(ChannexClient channexClient,
                             PropertyRepository propertyRepository,
                             RoomTypeRepository roomTypeRepository,
                             RatePlanRepository ratePlanRepository,
                             RoomAllotmentMonthlyRepository allotmentRepository,
                             RoomRateMonthlyRepository rateRepository,
                             ObjectMapper objectMapper) {
        this.channexClient = channexClient;
        this.propertyRepository = propertyRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.ratePlanRepository = ratePlanRepository;
        this.allotmentRepository = allotmentRepository;
        this.rateRepository = rateRepository;
        this.objectMapper = objectMapper;
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
    public void pullBookings() throws Exception {
        Map<String, Object> response = channexClient.getBookingRevisions();
        List<Map<String, Object>> revisions = (List<Map<String, Object>>) response.get("data");

        if (revisions == null) return;

        for (Map<String, Object> revision : revisions) {
            String revisionId = (String) revision.get("id");
            Map<String, Object> booking = (Map<String, Object>) revision.get("booking");
            
            try {
                processBooking(booking);
                channexClient.acknowledgeBookingRevision(revisionId);
            } catch (Exception e) {
                // Log error and continue with next revision
                e.printStackTrace();
            }
        }
    }

    private void processBooking(Map<String, Object> channexBooking) throws Exception {
        // Map Channex booking to ReservationEntity
        // This is a simplified version. A real implementation would need to:
        // 1. Find the PMS property, room type, and rate plan by Channex IDs
        // 2. Create or update the ReservationEntity
        
        String channexPropertyId = (String) channexBooking.get("property_id");
        // ... more mapping logic ...
    }

    @Transactional
    public void syncProperty(String propertyId) throws Exception {
        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        Map<String, Object> data = objectMapper.readValue(property.getDataJson(), new TypeReference<>() {});
        
        if (data.containsKey("channex_id")) {
            return; // Already synced
        }

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
        
        if (data.containsKey("channex_id")) {
            return; // Already synced
        }

        String propertyId = (String) data.get("property_id");
        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        Map<String, Object> propData = objectMapper.readValue(property.getDataJson(), new TypeReference<>() {});
        String channexPropertyId = (String) propData.get("channex_id");

        if (channexPropertyId == null) {
            throw new RuntimeException("Property must be synced to Channex first");
        }

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
        
        if (data.containsKey("channex_id")) {
            return; // Already synced
        }

        String roomTypeId = (String) data.get("room_type_id");
        RoomTypeEntity roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Room type not found"));
        Map<String, Object> rtData = objectMapper.readValue(roomType.getDataJson(), new TypeReference<>() {});
        String channexRoomTypeId = (String) rtData.get("channex_id");

        if (channexRoomTypeId == null) {
            throw new RuntimeException("Room type must be synced to Channex first");
        }

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
