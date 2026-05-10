package com.pms.backend.api;

import com.pms.backend.api.service.GuestService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/guests")
public class GuestController extends AbstractRestEntityController {
    public GuestController(GuestService service) {
        super(service);
    }
}
