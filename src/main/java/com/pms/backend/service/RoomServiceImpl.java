package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.RoomService;
import com.pms.backend.model.RoomEntity;
import com.pms.backend.repository.RoomRepository;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl extends AbstractRestEntityServiceImpl<RoomEntity> implements RoomService {
    public RoomServiceImpl(RoomRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, RoomEntity::new);
    }
}
