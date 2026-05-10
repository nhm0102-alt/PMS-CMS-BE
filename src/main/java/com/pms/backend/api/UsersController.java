package com.pms.backend.api;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class UsersController {
    @PostMapping({"/api/users/invite", "/users/invite"})
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> inviteUser(@RequestBody Map<String, Object> body) {
        String email = body.get("email") == null ? null : body.get("email").toString();
        String role = body.get("role") == null ? null : body.get("role").toString();
        return Map.of(
                "invited", true,
                "email", email,
                "role", role == null || role.isBlank() ? "user" : role
        );
    }
}

