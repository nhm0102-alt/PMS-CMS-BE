package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.UserService;
import com.pms.backend.model.UserEntity;
import com.pms.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends AbstractRestEntityServiceImpl<UserEntity> implements UserService {
    public UserServiceImpl(UserRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, UserEntity::new);
    }
}
