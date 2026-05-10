package com.pms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.api.service.FolioTransactionService;
import com.pms.backend.model.FolioTransactionEntity;
import com.pms.backend.repository.FolioTransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class FolioTransactionServiceImpl extends AbstractRestEntityServiceImpl<FolioTransactionEntity> implements FolioTransactionService {
    public FolioTransactionServiceImpl(FolioTransactionRepository repository, ObjectMapper objectMapper) {
        super(repository, objectMapper, FolioTransactionEntity::new);
    }
}
