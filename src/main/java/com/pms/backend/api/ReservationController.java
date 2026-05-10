package com.pms.backend.api;

import com.pms.backend.api.service.ReservationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ReservationController extends AbstractRestEntityController {
    public ReservationController(ReservationService service) {
        super(service);
    }
}
