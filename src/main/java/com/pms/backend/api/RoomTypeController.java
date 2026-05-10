package com.pms.backend.api;

import com.pms.backend.api.service.RoomTypeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/room-types")
public class RoomTypeController extends AbstractRestEntityController {
    public RoomTypeController(RoomTypeService service) {
        super(service);
    }
}
