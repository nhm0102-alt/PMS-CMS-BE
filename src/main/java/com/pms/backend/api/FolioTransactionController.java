package com.pms.backend.api;

import com.pms.backend.api.service.FolioTransactionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/folio-transactions")
public class FolioTransactionController extends AbstractRestEntityController {
    public FolioTransactionController(FolioTransactionService service) {
        super(service);
    }
}
