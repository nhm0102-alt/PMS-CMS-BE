package com.pms.backend.api;

import com.pms.backend.api.service.OTAChannelService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ota-channels")
public class OTAChannelController extends AbstractRestEntityController {
    public OTAChannelController(OTAChannelService service) {
        super(service);
    }
}
