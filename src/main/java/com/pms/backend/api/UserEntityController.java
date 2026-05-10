package com.pms.backend.api;

import com.pms.backend.api.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserEntityController extends AbstractRestEntityController {
    public UserEntityController(UserService service) {
        super(service);
    }
}
