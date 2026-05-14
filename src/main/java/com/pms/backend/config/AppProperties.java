package com.pms.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Cors cors,
        Auth auth,
        Channex channex
) {
    public record Channex(
            String apiKey,
            String baseUrl
    ) {
    }

    public record Jwt(
            String secret,
            String issuer,
            long expirationSeconds
    ) {
    }

    public record Cors(
            String allowedOrigins
    ) {
    }

    public record Auth(
            String username,
            String password
    ) {
    }
}
