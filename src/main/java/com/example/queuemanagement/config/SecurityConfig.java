package com.example.queuemanagement.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF (Common cause of 403 for POST requests)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. Disable Default Login Forms (Prevents redirects to /login)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            // 3. Configure CORS (Allows React Frontend to connect)
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:3000")); // Your React URL
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow All Actions
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            
            // 4. Define Access Rules (Open the specific endpoints)
            .authorizeHttpRequests(auth -> auth
                // ✅ OPEN ACCESS to these endpoints
                .requestMatchers("/api/admin/**").permitAll() 
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/queue/**").permitAll() // Fixes your Booking & Status 403
                .requestMatchers("/api/ai/**").permitAll()
                .requestMatchers("/api/prescription/**").permitAll()
                
                // 🔒 Lock everything else
                .anyRequest().authenticated()
            );

        return http.build();
    }
}