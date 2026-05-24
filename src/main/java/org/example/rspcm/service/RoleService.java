package org.example.rspcm.service;

import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.model.entity.Role;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final AppRoleRepository roleRepository;

    public Set<Role> resolveRoles(Set<RoleName> roleNames) {
        return roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new ErrorMessageException("Роль не найдена: " + name, ErrorCodes.NotFound)))
                .collect(Collectors.toSet());
    }
}
