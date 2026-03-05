package com.example.capstone_project.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import com.example.capstone_project.filter.JwtAuthFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Slf4j
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    public SecurityConfig(JwtAuthFilter jwtAuthFilter)
    {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        Logger logger=LoggerFactory.getLogger(SecurityConfig.class);
        logger.warn("PERMITTING THE ENDPOINTS BASED ON ROLES .....");
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/capstone/v1/auth/**").permitAll()
                        .requestMatchers("/api/capstone/v1/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/capstone/v1/user/document/updateDocumentStatus").hasAuthority("APPROVER")
                        .requestMatchers("/api/capstone/v1/user/document/upload",
                                "/api/capstone/v1/user/document/viewAllDocuments",
                                "/api/capstone/v1/user/document/viewDocument").hasAnyAuthority("ADMIN","CREATOR")
                        .requestMatchers("/api/capstone/v1/user/document/removeDocument").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
