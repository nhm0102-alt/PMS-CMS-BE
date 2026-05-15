package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.RoomTypeService;
import com.pms.backend.model.RoomTypeEntity;
import com.pms.backend.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class RoomTypeServiceImpl extends AbstractRestEntityServiceImpl<RoomTypeEntity> implements RoomTypeService {
    private final ChannexSyncService channexSyncService;

    public RoomTypeServiceImpl(RoomTypeRepository repository, ObjectMapper objectMapper, ChannexSyncService channexSyncService) {
        super(repository, objectMapper, RoomTypeEntity::new);
        this.channexSyncService = channexSyncService;
    }

    @Override
    protected void syncEntityWithData(RoomTypeEntity entity, Map<String, Object> data) {
        if (data != null) {
            entity.setPropertyId((String) data.get("property_id"));
        }
    }

    @Override
    protected void afterSave(RoomTypeEntity entity, Map<String, Object> data) {
        try {
            channexSyncService.syncRoomType(entity.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
