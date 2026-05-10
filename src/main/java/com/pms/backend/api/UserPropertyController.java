package com.pms.backend.api;

import com.pms.backend.api.service.UserPropertyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-properties")
public class UserPropertyController extends AbstractRestEntityController {
    public UserPropertyController(UserPropertyService service) {
        super(service);
    }
}
