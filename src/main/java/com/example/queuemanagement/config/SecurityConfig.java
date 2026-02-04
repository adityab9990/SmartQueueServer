package com.example.queuemanagement.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF (Critical for POST/PUT/DELETE)
            .csrf(csrf -> csrf.disable()) 
            
            // 2. Configure CORS (Critical for React Frontend)
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:3000")); // Your React Port
                
                // ✅ MUST INCLUDE "DELETE" for Admin Dashboard
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            
            // 3. Define Access Rules
            .authorizeHttpRequests(auth -> auth
                // ✅ UNLOCK ALL CONTROLLERS
                .requestMatchers("/api/admin/**").permitAll() 
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/queue/**").permitAll()
                .requestMatchers("/api/ai/**").permitAll()
                .requestMatchers("/api/prescription/**").permitAll()
                // Lock everything else
                .anyRequest().authenticated()
            );

        return http.build();
    }
}