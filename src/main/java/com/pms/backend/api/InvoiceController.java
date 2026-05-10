package com.pms.backend.api;

import com.pms.backend.api.service.InvoiceService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoices")
public class InvoiceController extends AbstractRestEntityController {
    public InvoiceController(InvoiceService service) {
        super(service);
    }
}
