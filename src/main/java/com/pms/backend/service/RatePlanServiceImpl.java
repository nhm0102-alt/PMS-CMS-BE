package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.RatePlanService;
import com.pms.backend.model.RatePlanEntity;
import com.pms.backend.repository.RatePlanRepository;
import org.springframework.stereotype.Service;

@Service
public class RatePlanServiceImpl extends AbstractRestEntityServiceImpl<RatePlanEntity> implements RatePlanService {
    public RatePlanServiceImpl(RatePlanRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, RatePlanEntity::new);
    }
}
