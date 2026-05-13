package org.example.rspcm.service;

import org.example.rspcm.config.AppProperties;
import org.example.rspcm.dto.auth.AuthResponse;
import org.example.rspcm.dto.auth.LoginRequest;
import org.example.rspcm.dto.auth.RegisterRequest;
import org.example.rspcm.dto.auth.VerifyOtpRequest;
import org.example.rspcm.dto.user.UserResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.entity.OtpVerification;
import org.example.rspcm.mapper.AuthMapper;
import org.example.rspcm.mapper.UserMapper;
import org.example.rspcm.repository.OtpVerificationRepository;
import org.example.rspcm.repository.UserRepository;
import org.example.rspcm.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;
    private final AppProperties appProperties;
    private final UserProfileSyncService userProfileSyncService;
    private final Random random = new Random();
    private final UserDetailsService userDetailsService;

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ErrorMessageException("Email allaqachon mavjud", ErrorCodes.AlreadyExists);
        }

        User user = authMapper.toUserEntity(
                request,
                passwordEncoder.encode(request.password()),
                roleService.resolveRoles(request.roles())
        );

        User saved = userRepository.save(user);
        userProfileSyncService.sync(saved);
        sendOtp(request.email());

        return "Ro'yxatdan o'tish yakunlandi. OTP emailingizga yuborildi.";
    }

    @Transactional
    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));

        if (user.isEnabled()) {
            throw new ErrorMessageException("Bu akkaunt allaqachon tasdiqlangan", ErrorCodes.AlreadyExists);
        }

        sendOtp(email);
        return "Yangi OTP yuborildi.";
    }

    @Transactional
    public String verifyOtp(VerifyOtpRequest request) {
        OtpVerification otp = otpRepository
                .findFirstByEmailAndCodeAndUsedFalseOrderByIdDesc(request.email(), request.code())
                .orElseThrow(() -> new ErrorMessageException("OTP noto'g'ri", ErrorCodes.InvalidParams));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ErrorMessageException("OTP muddati tugagan", ErrorCodes.InvalidParams);
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        user.setEnabled(true);
        otp.setUsed(true);
        userRepository.save(user);
        otpRepository.save(otp);

        return "Akkaunt muvaffaqiyatli tasdiqlandi.";
    }

    public AuthResponse login(LoginRequest request) {

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        if(!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
            throw new ErrorMessageException("Email yoki parol noto'g'ri", ErrorCodes.InvalidParams);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authMapper.toAuthResponse(
                userDetails.getUsername(),
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()),
                jwtService.generateToken(userDetails)
        );
    }

    private void sendOtp(String email) {
        String code = "%06d".formatted(random.nextInt(1_000_000));

        OtpVerification otp = OtpVerification.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(appProperties.getOtp().getExpirationMinutes()))
                .used(false)
                .build();

        otpRepository.save(otp);
        mailService.sendOtp(email, code);
    }

    public UserResponse getUser(User user) {
        return userMapper.toResponse(user);
    }
}
