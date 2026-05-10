package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.PolicyService;
import com.pms.backend.model.PolicyEntity;
import com.pms.backend.repository.PolicyRepository;
import org.springframework.stereotype.Service;

@Service
public class PolicyServiceImpl extends AbstractRestEntityServiceImpl<PolicyEntity> implements PolicyService {
    public PolicyServiceImpl(PolicyRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, PolicyEntity::new);
    }
}
