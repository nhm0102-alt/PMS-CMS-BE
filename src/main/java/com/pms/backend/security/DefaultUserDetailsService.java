package com.pms.backend.security;

import com.pms.backend.config.AppProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DefaultUserDetailsService implements UserDetailsService {
    private final String username;
    private final UserDetails user;

    public DefaultUserDetailsService(AppProperties properties, PasswordEncoder passwordEncoder) {
        AppProperties.Auth auth = properties.auth();
        this.username = auth.username();
        this.user = User.withUsername(auth.username())
                .password(passwordEncoder.encode(auth.password()))
                .roles("ADMIN")
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!this.username.equals(username)) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }
}
