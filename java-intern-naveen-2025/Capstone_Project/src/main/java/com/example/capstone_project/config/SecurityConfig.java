package com.example.capstone_project.config;

import com.example.capstone_project.filter.JwtAuthFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Main API security filter chain (Order 2, after the Authorization Server chain).
     *
     * <p>Supports two authentication mechanisms:
     * <ol>
     *   <li><b>Custom JWT</b> – tokens issued by {@code /api/capstone/v1/auth/login} and
     *       validated by {@link JwtAuthFilter} (HMAC-SHA256, existing behaviour).</li>
     *   <li><b>OAuth2 JWT</b> – tokens issued by the Spring Authorization Server at
     *       {@code /oauth2/token} and validated by the built-in OAuth2 Resource Server
     *       (RSA-signed, new behaviour).</li>
     * </ol>
     */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
        logger.warn("PERMITTING THE ENDPOINTS BASED ON ROLES .....");

        http
                // CSRF protection is disabled because this API is stateless (no session cookies).
                // Tokens are transmitted via the Authorization header, making CSRF attacks irrelevant.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints – no token required
                        .requestMatchers("/api/capstone/v1/auth/**").permitAll()
                        // Admin-only endpoints
                        .requestMatchers("/api/capstone/v1/admin/**").hasAuthority("ADMIN")
                        // Approver-only endpoint – document status approval is a business action;
                        // only the APPROVER role (custom JWT) or admin.access scope (OAuth2) may perform it
                        .requestMatchers("/api/capstone/v1/user/document/updateDocumentStatus").hasAnyAuthority("APPROVER", "SCOPE_admin.access")
                        // Document read/upload – ADMIN or CREATOR (custom JWT) OR OAuth2 scopes
                        .requestMatchers(
                                "/api/capstone/v1/user/document/upload",
                                "/api/capstone/v1/user/document/viewAllDocuments",
                                "/api/capstone/v1/user/document/viewDocument"
                        ).hasAnyAuthority("ADMIN", "CREATOR", "SCOPE_document.read", "SCOPE_document.write")
                        // Document removal – ADMIN only
                        .requestMatchers("/api/capstone/v1/user/document/removeDocument").hasAnyAuthority("ADMIN", "SCOPE_admin.access")
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Custom JWT filter (HMAC-signed tokens from /auth/login)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // OAuth2 Resource Server (RSA-signed tokens from /oauth2/token)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

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
