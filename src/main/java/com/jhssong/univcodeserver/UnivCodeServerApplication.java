package com.jhssong.univcodeserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UnivCodeServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnivCodeServerApplication.class, args);
    }
}
