package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.OTAChannelService;
import com.pms.backend.model.OTAChannelEntity;
import com.pms.backend.repository.OTAChannelRepository;
import org.springframework.stereotype.Service;

@Service
public class OTAChannelServiceImpl extends AbstractRestEntityServiceImpl<OTAChannelEntity> implements OTAChannelService {
    public OTAChannelServiceImpl(OTAChannelRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, OTAChannelEntity::new);
    }
}
