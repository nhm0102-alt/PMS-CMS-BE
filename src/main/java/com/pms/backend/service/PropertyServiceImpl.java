package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.PropertyService;
import com.pms.backend.model.PropertyEntity;
import com.pms.backend.repository.PropertyRepository;
import org.springframework.stereotype.Service;

@Service
public class PropertyServiceImpl extends AbstractRestEntityServiceImpl<PropertyEntity> implements PropertyService {
    public PropertyServiceImpl(PropertyRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, PropertyEntity::new);
    }
}
