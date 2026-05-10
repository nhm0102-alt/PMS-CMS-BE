package com.pms.backend.security;

import com.pms.backend.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final AppProperties.Jwt jwt;
    private final SecretKey key;

    public JwtService(AppProperties properties) {
        this.jwt = properties.jwt();
        this.key = buildKey(jwt.secret());
    }

    public String generateToken(UserDetails user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwt.expirationSeconds());
        List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(jwt.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("roles", roles)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey buildKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret must be set");
        }

        byte[] keyBytes;
        boolean maybeBase64 = secret.matches("^[A-Za-z0-9+/=]+$") && secret.length() % 4 == 0;
        if (maybeBase64) {
            try {
                keyBytes = Decoders.BASE64.decode(secret);
            } catch (Exception ex) {
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            }
        } else {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes for HS256");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
