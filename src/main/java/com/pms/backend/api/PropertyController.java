package com.pms.backend.api;

import com.pms.backend.api.service.PropertyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/properties")
public class PropertyController extends AbstractRestEntityController {
    public PropertyController(PropertyService service) {
        super(service);
    }
}
