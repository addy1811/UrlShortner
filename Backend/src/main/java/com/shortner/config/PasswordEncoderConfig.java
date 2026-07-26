package com.shortner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Default strength (10 rounds) - a reasonable cost/latency tradeoff for a
        // login endpoint; bump to 12 only if you've benchmarked the login latency hit.
        return new BCryptPasswordEncoder();
    }
}