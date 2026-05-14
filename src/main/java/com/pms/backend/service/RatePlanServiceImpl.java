package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.RatePlanService;
import com.pms.backend.model.RatePlanEntity;
import com.pms.backend.repository.RatePlanRepository;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class RatePlanServiceImpl extends AbstractRestEntityServiceImpl<RatePlanEntity> implements RatePlanService {
    private final ChannexSyncService channexSyncService;

    public RatePlanServiceImpl(RatePlanRepository repository, ObjectMapper objectMapper, ChannexSyncService channexSyncService) {
        super(repository, objectMapper, RatePlanEntity::new);
        this.channexSyncService = channexSyncService;
    }

    @Override
    protected void afterSave(RatePlanEntity entity, Map<String, Object> data) {
        try {
            channexSyncService.syncRatePlan(entity.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
