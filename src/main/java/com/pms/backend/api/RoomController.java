package com.pms.backend.api;

import com.pms.backend.api.service.RoomService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
public class RoomController extends AbstractRestEntityController {
    public RoomController(RoomService service) {
        super(service);
    }
}
