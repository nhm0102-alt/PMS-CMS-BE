package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.RoomTypeService;
import com.pms.backend.model.RoomTypeEntity;
import com.pms.backend.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class RoomTypeServiceImpl extends AbstractRestEntityServiceImpl<RoomTypeEntity> implements RoomTypeService {
    public RoomTypeServiceImpl(RoomTypeRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, RoomTypeEntity::new);
    }
}
