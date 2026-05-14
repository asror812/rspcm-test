package org.example.rspcm.mapper;

import org.example.rspcm.dto.auth.AuthResponse;
import org.example.rspcm.dto.auth.RegisterRequest;
import org.example.rspcm.model.entity.Role;
import org.example.rspcm.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuthMapper {

    public User toUserEntity(RegisterRequest request, String encodedPassword, Set<Role> roles) {
        return User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(encodedPassword)
                .enabled(false)
                .roles(roles)
                .build();
    }

    public AuthResponse toAuthResponse(String email, Set<String> roles, String token) {
        return new AuthResponse(email, roles, token);
    }
}
