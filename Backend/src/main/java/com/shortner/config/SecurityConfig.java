package com.shortner.config;

import com.shortner.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

      @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // stateless JWT API, no session/cookie-based CSRF surface
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints must be reachable before a token exists.
                .requestMatchers("/api/auth/**").permitAll()
 
                // Redirect resolution is public at the HTTP layer - actual visibility/grant
                // enforcement (PUBLIC/PRIVATE/RESTRICTED) happens inside AccessControlService,
                // not here, since it depends on per-link data, not just "logged in or not".
                .requestMatchers("/r/**").permitAll()
 
                // JSON counterpart to /r/** used by the frontend's redirect-resolver page -
                // same reasoning: PUBLIC links must resolve with no token, PRIVATE/RESTRICTED
                // links still get correctly rejected inside AccessControlService if the
                // (optional) JWT on the request doesn't grant access.
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/links/resolve/*").permitAll()
 
                // Form schema viewing (GET) and visitor submission (POST .../submit) are
                // public - anonymous visitors need both. Defining the form's fields
                // (POST .../form, owner only) is deliberately NOT in this list - it falls
                // through to anyRequest().authenticated() below. Scoping by HttpMethod here
                // matters: without it, permitAll on the path would also expose the
                // owner-only POST that defines/replaces the form schema.
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/links/*/form").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/links/*/form/submit").permitAll()
 
                .requestMatchers(
                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                ).permitAll()
 
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }
 
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
 
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
 
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
 
        List<String> origins = allowedOrigins.isBlank()
            ? List.of()
            : List.of(allowedOrigins.split(","));
 
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
 
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}