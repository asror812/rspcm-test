package org.example.rspcm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RspcmApplication {

    public static void main(String[] args) {
        SpringApplication.run(RspcmApplication.class, args);
    }
}
