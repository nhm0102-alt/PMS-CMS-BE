package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.LicenseTransactionService;
import com.pms.backend.model.LicenseTransactionEntity;
import com.pms.backend.repository.LicenseTransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class LicenseTransactionServiceImpl extends AbstractRestEntityServiceImpl<LicenseTransactionEntity> implements LicenseTransactionService {
    public LicenseTransactionServiceImpl(LicenseTransactionRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, LicenseTransactionEntity::new);
    }
}
