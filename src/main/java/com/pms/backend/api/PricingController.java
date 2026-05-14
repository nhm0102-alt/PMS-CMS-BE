package com.pms.backend.api;

import com.pms.backend.api.service.PricingService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pricing")
public class PricingController {
    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/inventory")
    public Map<String, Object> getInventory(@RequestParam String property_id,
                                          @RequestParam String start_date,
                                          @RequestParam(defaultValue = "21") int days) {
        return pricingService.getInventory(property_id, start_date, days);
    }

    @PostMapping("/inventory")
    public void updateInventory(@RequestParam String property_id,
                               @RequestBody Map<String, Object> body) {
        pricingService.updateInventory(property_id, body);
    }
}
