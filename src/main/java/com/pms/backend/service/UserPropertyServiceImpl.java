package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.UserPropertyService;
import com.pms.backend.model.UserPropertyEntity;
import com.pms.backend.repository.UserPropertyRepository;
import org.springframework.stereotype.Service;

@Service
public class UserPropertyServiceImpl extends AbstractRestEntityServiceImpl<UserPropertyEntity> implements UserPropertyService {
    public UserPropertyServiceImpl(UserPropertyRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, UserPropertyEntity::new);
    }
}
