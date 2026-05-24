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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final UserMapper userMapper;

    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAllBy(pageable).map(userMapper::toResponse);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    public UserResponse findResponseById(Long id) {
        return userMapper.toResponse(userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id)));
    }

    public UserResponse createResponse(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ErrorMessageException("Email уже существует", ErrorCodes.AlreadyExists);
        }

        User user = userMapper.toEntity(
                request,
                passwordEncoder.encode(request.password()),
                roleService.resolveRoles(request.roles())
        );

        User saved = userRepository.save(user);
        userProfileSyncService.sync(saved);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findById(id);
        userMapper.updateEntity(user, request, roleService.resolveRoles(request.roles()));
        User saved = userRepository.save(user);
        userProfileSyncService.sync(saved);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }

    public UserResponse getMe(User user) {
        return userMapper.toResponse(user);
    }
}
