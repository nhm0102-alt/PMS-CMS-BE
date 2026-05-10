package com.pms.backend.api;

import com.pms.backend.api.service.PolicyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/policies")
public class PolicyController extends AbstractRestEntityController {
    public PolicyController(PolicyService service) {
        super(service);
    }
}
