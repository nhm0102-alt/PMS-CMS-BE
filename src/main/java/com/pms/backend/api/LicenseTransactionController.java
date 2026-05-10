package com.pms.backend.api;

import com.pms.backend.api.service.LicenseTransactionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/license-transactions")
public class LicenseTransactionController extends AbstractRestEntityController {
    public LicenseTransactionController(LicenseTransactionService service) {
        super(service);
    }
}
