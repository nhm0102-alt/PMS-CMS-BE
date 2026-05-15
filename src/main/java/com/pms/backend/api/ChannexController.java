package com.pms.backend.api;

import com.pms.backend.service.ChannexSyncService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channex")
public class ChannexController {
    private final ChannexSyncService channexSyncService;

    public ChannexController(ChannexSyncService channexSyncService) {
        this.channexSyncService = channexSyncService;
    }

    @PostMapping("/sync-all")
    public String syncAll() {
        try {
            channexSyncService.syncAllData();
            return "Sync started successfully";
        } catch (Exception e) {
            return "Sync failed: " + e.getMessage();
        }
    }
}
