package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.GuestService;
import com.pms.backend.model.GuestEntity;
import com.pms.backend.repository.GuestRepository;
import org.springframework.stereotype.Service;

@Service
public class GuestServiceImpl extends AbstractRestEntityServiceImpl<GuestEntity> implements GuestService {
    public GuestServiceImpl(GuestRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, GuestEntity::new);
    }
}
