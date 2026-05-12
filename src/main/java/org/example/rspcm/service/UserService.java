package org.example.rspcm.service;

import lombok.extern.slf4j.Slf4j;
import org.example.rspcm.dto.user.UserCreateRequest;
import org.example.rspcm.dto.user.UserResponse;
import org.example.rspcm.dto.user.UserUpdateRequest;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.mapper.UserMapper;
import org.example.rspcm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileSyncService userProfileSyncService;

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserMapper::toResponse).toList();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User topilmadi: " + id));
    }

    public UserResponse findResponseById(Long id) {
        return UserMapper.toResponse(userRepository.findById(id).orElseThrow(() -> new NotFoundException("User topilmadi: " + id)));
    }

    @Transactional
    public User create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ErrorMessageException("Email allaqachon mavjud", ErrorCodes.AlreadyExists);
        }
        User user = UserMapper.toEntity(
                request,
                passwordEncoder.encode(request.password()),
                roleService.resolveRoles(request.roles())
        );
        User saved = userRepository.save(user);
        userProfileSyncService.sync(saved);
        return saved;
    }

    public UserResponse createResponse(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ErrorMessageException("Email allaqachon mavjud", ErrorCodes.AlreadyExists);
        }
        User user = UserMapper.toEntity(
                request,
                passwordEncoder.encode(request.password()),
                roleService.resolveRoles(request.roles())
        );
        User saved = userRepository.save(user);
        userProfileSyncService.sync(saved);
        return UserMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findById(id);
        UserMapper.updateEntity(user, request, roleService.resolveRoles(request.roles()));
        User saved = userRepository.save(user);
        userProfileSyncService.sync(saved);
        return UserMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}
