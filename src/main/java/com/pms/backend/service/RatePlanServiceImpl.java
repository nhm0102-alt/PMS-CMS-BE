package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.RatePlanService;
import com.pms.backend.model.RatePlanEntity;
import com.pms.backend.repository.RatePlanRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;

@Service
public class RatePlanServiceImpl extends AbstractRestEntityServiceImpl<RatePlanEntity> implements RatePlanService {
    private final ChannexSyncService channexSyncService;
    private final JdbcTemplate jdbcTemplate;

    public RatePlanServiceImpl(RatePlanRepository repository, ObjectMapper objectMapper, ChannexSyncService channexSyncService, JdbcTemplate jdbcTemplate) {
        super(repository, objectMapper, RatePlanEntity::new);
        this.channexSyncService = channexSyncService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void syncEntityWithData(RatePlanEntity entity, Map<String, Object> data) {
        if (data != null) {
            entity.setPropertyId((String) data.remove("property_id"));
            entity.setChannexId((String) data.remove("channex_id"));
            entity.setCancellationPolicyId((String) data.remove("cancellation_policy_id"));
            entity.setSurchargePolicyId((String) data.remove("surcharge_policy_id"));
            
            if (data.containsKey("room_type_ids")) {
                entity.setRoomTypeIds((List<String>) data.remove("room_type_ids"));
            }
        }
    }

    @Override
    protected void postProcessResponse(RatePlanEntity entity, Map<String, Object> response) {
        response.put("property_id", entity.getPropertyId());
        response.put("channex_id", entity.getChannexId());
        response.put("cancellation_policy_id", entity.getCancellationPolicyId());
        response.put("surcharge_policy_id", entity.getSurchargePolicyId());
        
        if (entity.getRoomTypeIds() == null) {
            entity.setRoomTypeIds(repository.findRoomTypeIdsByRatePlanId(entity.getId()));
        }
        response.put("room_type_ids", entity.getRoomTypeIds());
    }

    @Override
    protected void afterSave(RatePlanEntity entity, Map<String, Object> data) {
        List<String> roomTypeIds = entity.getRoomTypeIds();
        if (roomTypeIds != null) {
            jdbcTemplate.update("DELETE FROM rate_plan_room_types WHERE rate_plan_id = ?", entity.getId());
            for (String roomTypeId : roomTypeIds) {
                if (roomTypeId != null && !roomTypeId.isBlank()) {
                    jdbcTemplate.update("INSERT INTO rate_plan_room_types (rate_plan_id, room_type_id) VALUES (?, ?)", 
                        entity.getId(), roomTypeId);
                }
            }
        }
        try {
            channexSyncService.syncRatePlan(entity.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
