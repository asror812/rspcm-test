package org.example.rspcm.mapper;

import org.example.rspcm.dto.auth.AuthResponse;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuthMapper {

    public AuthResponse toAuthResponse(String email, Set<String> roles, String token) {
        return new AuthResponse(email, roles, token);
    }
}
