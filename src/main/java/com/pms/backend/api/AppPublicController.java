package com.pms.backend.api;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/apps/public")
public class AppPublicController {
    @GetMapping("/prod/public-settings/by-id/{appId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> publicSettings(
            @PathVariable String appId,
            @RequestHeader(value = "X-App-Id", required = false) String headerAppId
    ) {
        String id = headerAppId != null && !headerAppId.isBlank() ? headerAppId : appId;
        return Map.of(
                "id", id,
                "public_settings", Map.of("requires_auth", false)
        );
    }
}

