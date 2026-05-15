package org.example.rspcm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.repository.UserRepository;
import org.example.rspcm.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Bean
    public OpenAPI openAPI() {
        String schemeName = "bearerAuth";

        String adminToken = generateAdminToken();
        String teacherToken = generateTeacherToken();

        return new OpenAPI()
                .info(new Info()
                        .title("RSPCM API")
                        .version("v1")
                        .description("""
                                ### Admin JWT token
                                ```
                                %s
                                ```
                                
                                ### Teacher JWT token
                                ```
                                %s
                                ```
                                
                                
                                """.formatted(adminToken, teacherToken))
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development server"),
                        new Server().url("https://api.rspcm.uz").description("Production server")
                ))

                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(
                                schemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }

    private String generateTeacherToken() {
        Optional<User> existingUser = userRepository.findByEmail("math.teacher@rspcm.local");
        return existingUser.map(this::toUserDetails)
                .map(jwtService::generateToken).orElse(null);
    }

    private String generateAdminToken() {
        Optional<User> existingUser = userRepository.findByEmail("admin@rspcm.local");
        return existingUser.map(this::toUserDetails)
                .map(jwtService::generateToken).orElse(null);
    }

    private UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .toList())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}
