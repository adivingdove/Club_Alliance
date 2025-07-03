package com.example.uclub_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

   @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                          .requestMatchers("/ws-chat/**", "/webjars/**", "/sockjs-node/**", "/info", "/info/**").permitAll()
                           .requestMatchers("/api/auth/**").permitAll()
                           .requestMatchers("/api/user/**").permitAll()
                           .requestMatchers("/uploads/**").permitAll()
                           .requestMatchers("/api/upload").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/user/login").permitAll()
                        .requestMatchers("/api/user/register").permitAll()
                        .requestMatchers("/api/user/sendRegisterCode").permitAll()
                        .requestMatchers("/api/user/sendResetCode").permitAll()
                        .requestMatchers("/api/user/resetPassword").permitAll()
                        .requestMatchers("/api/user/check").permitAll()
                        .requestMatchers("/api/clubs").permitAll()
                        .requestMatchers("/api/clubs/active").permitAll()
                        .requestMatchers("/api/clubs/{id}").permitAll()
                        .requestMatchers("/api/clubs/{id}/detail").permitAll()
                        .requestMatchers("/api/clubs/hot").permitAll()
                        .requestMatchers("/api/clubs/search").permitAll()
                        .requestMatchers("/api/activities").permitAll()
                        .requestMatchers("/api/activities/upcoming").permitAll()
                        .requestMatchers("/api/activities/{id}").permitAll()
                        .requestMatchers("/api/activities/search").permitAll()
                        .requestMatchers("/api/activities/club/{clubId}/approved").permitAll()
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/ai/ask").permitAll()
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers("/api/report/**").permitAll()
                       .requestMatchers("/api/comments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/announcements").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/clubs/*/members/*").authenticated()
                        .anyRequest().authenticated()
                        
                )
                 .csrf(csrf -> csrf.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}