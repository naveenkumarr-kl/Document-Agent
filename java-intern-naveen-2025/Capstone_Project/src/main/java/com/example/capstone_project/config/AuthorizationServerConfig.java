package com.example.capstone_project.config;

import com.example.capstone_project.entity.User;
import com.example.capstone_project.repository.UserRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OAuth 2.0 Authorization Server configuration.
 *
 * <p>Exposes standard OAuth 2.0 / OIDC endpoints:
 * <ul>
 *   <li>POST {@code /oauth2/token}           – Token endpoint (client_credentials, authorization_code, refresh_token)</li>
 *   <li>GET  {@code /oauth2/authorize}        – Authorization endpoint (Authorization Code + PKCE)</li>
 *   <li>POST {@code /oauth2/revoke}           – Token revocation</li>
 *   <li>POST {@code /oauth2/introspect}       – Token introspection</li>
 *   <li>GET  {@code /.well-known/openid-configuration} – OIDC Discovery</li>
 *   <li>GET  {@code /oauth2/jwks}             – JWK Set (public keys)</li>
 * </ul>
 *
 * <h3>Registered client: {@code document-management-client}</h3>
 * <ul>
 *   <li>Supports Authorization Code + PKCE (user-facing web/SPA clients)</li>
 *   <li>Supports Client Credentials (service-to-service)</li>
 *   <li>Supports Refresh Token</li>
 *   <li>Scopes: {@code openid}, {@code profile}, {@code document.read}, {@code document.write}, {@code admin.access}</li>
 * </ul>
 */
@Configuration
public class AuthorizationServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServerConfig.class);

    /**
     * Authorization Server security filter chain (highest priority – Order 1).
     * Handles all OAuth2/OIDC protocol endpoints and protects them with
     * the default Spring Authorization Server security rules.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // Enable OpenID Connect 1.0
                .oidc(Customizer.withDefaults());

        http
                // Redirect unauthenticated users to the login endpoint
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/api/capstone/v1/auth/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // Accept access tokens for the UserInfo endpoint
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        logger.info("OAuth2 Authorization Server security filter chain configured.");
        return http.build();
    }

    /**
     * In-memory registered client repository.
     *
     * <p>For production use, replace {@link InMemoryRegisteredClientRepository} with a
     * JDBC-backed implementation (e.g. {@code JdbcRegisteredClientRepository}).
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {

        RegisteredClient documentManagementClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("document-management-client")
                // In production: hash the secret with BCrypt and use the {bcrypt} prefix,
                // or store the client registration in the database. Never commit plaintext secrets.
                // Example: {bcrypt}$2a$10$...
                .clientSecret("{noop}document-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                // Supported grant types
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                // Allowed redirect URIs (update for your frontend URL)
                .redirectUri("http://localhost:8080/login/oauth2/code/document-management-client")
                .redirectUri("http://localhost:8080/authorized")
                // Scopes
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("document.read")
                .scope("document.write")
                .scope("admin.access")
                // Client settings: require PKCE + consent screen
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .requireProofKey(true)
                        .build())
                // Token settings
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .reuseRefreshTokens(false)
                        .build())
                .build();

        logger.info("Registered OAuth2 client: {}", documentManagementClient.getClientId());
        return new InMemoryRegisteredClientRepository(documentManagementClient);
    }

    /**
     * JWK source used by the Authorization Server to sign JWT access tokens.
     *
     * <p>An RSA-2048 key pair is generated at startup. For production deployments
     * persist and rotate keys (e.g. store in AWS Secrets Manager or a key store).
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey  publicKey  = (RSAPublicKey)  keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    /** Generates a fresh RSA-2048 key pair for token signing. */
    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair for OAuth2 token signing", ex);
        }
    }

    /**
     * JWT decoder used by the resource server portion of the Authorization Server
     * (e.g. the UserInfo endpoint) to validate its own access tokens.
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * Authorization Server settings. The issuer URI should match the base URL
     * of the application (configured via {@code spring.security.oauth2.authorizationserver.issuer}).
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    /**
     * OAuth2 JWT token customizer.
     *
     * <p>Enriches access tokens issued by the Authorization Server with
     * application-specific claims: {@code uid}, {@code role}, and {@code authorities}.
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(UserRepository userRepository) {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }

            Authentication principal = context.getPrincipal();
            String email = principal.getName();

            // Add granted authorities as a claim
            Set<String> authorities = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            context.getClaims().claim("authorities", authorities);

            // Add user-specific claims from the database
            User user = userRepository.findByEmail(email);
            if (user != null) {
                context.getClaims().claim("uid", user.getUid());
                if (user.getRole() != null) {
                    context.getClaims().claim("role", user.getRole().name());
                }
            }
        };
    }
}
