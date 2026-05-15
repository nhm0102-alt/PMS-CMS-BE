package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.PropertyService;
import com.pms.backend.model.PropertyEntity;
import com.pms.backend.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class PropertyServiceImpl extends AbstractRestEntityServiceImpl<PropertyEntity> implements PropertyService {
    private final ChannexSyncService channexSyncService;

    public PropertyServiceImpl(PropertyRepository repository, ObjectMapper objectMapper, ChannexSyncService channexSyncService) {
        super(repository, objectMapper, PropertyEntity::new);
        this.channexSyncService = channexSyncService;
    }

    @Override
    protected void syncEntityWithData(PropertyEntity entity, Map<String, Object> data) {
        if (data != null) {
            entity.setChannexId((String) data.remove("channex_id"));
        }
    }

    @Override
    protected void postProcessResponse(PropertyEntity entity, Map<String, Object> response) {
        response.put("channex_id", entity.getChannexId());
    }

    @Override
    protected void afterSave(PropertyEntity entity, Map<String, Object> data) {
        try {
            channexSyncService.syncProperty(entity.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
