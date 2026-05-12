package org.example.rspcm.mapper;

import org.example.rspcm.dto.auth.AuthResponse;
import org.example.rspcm.dto.auth.RegisterRequest;
import org.example.rspcm.model.entity.Role;
import org.example.rspcm.model.entity.User;

import java.util.Set;

public final class AuthMapper {
    private AuthMapper() {
    }

    public static User toUserEntity(RegisterRequest request, String encodedPassword, Set<Role> roles) {
        return User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(encodedPassword)
                .enabled(false)
                .roles(roles)
                .build();
    }

    public static AuthResponse toAuthResponse(String email, Set<String> roles, String token) {
        return new AuthResponse(email, roles, token);
    }
}
