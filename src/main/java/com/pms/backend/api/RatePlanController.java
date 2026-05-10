package com.pms.backend.api;

import com.pms.backend.api.service.RatePlanService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rate-plans")
public class RatePlanController extends AbstractRestEntityController {
    public RatePlanController(RatePlanService service) {
        super(service);
    }
}
