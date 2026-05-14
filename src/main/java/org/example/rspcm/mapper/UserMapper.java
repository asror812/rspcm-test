package org.example.rspcm.mapper;

import org.example.rspcm.dto.user.UserCreateRequest;
import org.example.rspcm.dto.user.UserResponse;
import org.example.rspcm.dto.user.UserUpdateRequest;
import org.example.rspcm.model.entity.Role;
import org.example.rspcm.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.isEnabled(), user.isDeleted(), roles);
    }

    public User toEntity(UserCreateRequest request, String encodedPassword, Set<Role> roles) {
        return User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(encodedPassword)
                .enabled(request.enabled())
                .roles(roles)
                .build();
    }

    public void updateEntity(User user, UserUpdateRequest request, Set<Role> roles) {
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(request.enabled());
        user.setRoles(roles);
    }
}
