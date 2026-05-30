package org.example.rspcm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
@Configuration
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Otp otp = new Otp();
    private Mail mail = new Mail();
    private Admin admin = new Admin();
    private Swagger swagger = new Swagger();
    private String uploadDir;

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMinutes;
    }

    @Getter
    @Setter
    public static class Otp {
        private long expirationMinutes;
    }

    @Getter
    @Setter
    public static class Mail {
        private String from;
    }

    @Getter
    @Setter
    public static class Admin {
        private boolean enabled;
        private String username;
        private String password;
        private String fullName;
    }

    @Getter
    @Setter
    public static class Swagger {
        private String panelToken;
    }

}
