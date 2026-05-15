package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.RoomService;
import com.pms.backend.model.RoomEntity;
import com.pms.backend.repository.RoomRepository;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class RoomServiceImpl extends AbstractRestEntityServiceImpl<RoomEntity> implements RoomService {
    public RoomServiceImpl(RoomRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, RoomEntity::new);
    }

    @Override
    protected void syncEntityWithData(RoomEntity entity, Map<String, Object> data) {
        if (data != null) {
            entity.setPropertyId((String) data.remove("property_id"));
            entity.setRoomTypeId((String) data.remove("room_type_id"));
        }
    }

    @Override
    protected void postProcessResponse(RoomEntity entity, Map<String, Object> response) {
        response.put("property_id", entity.getPropertyId());
        response.put("room_type_id", entity.getRoomTypeId());
    }
}
