package com.gdamiens.website.security;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.usage.UsageFilter;
import com.gdamiens.website.usage.UsageRecorder;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.source.JWKSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Value("${security.jwt.token.secret-key}")
    private String jwtSecretKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RateLimiter rateLimiter,
                                           ApplicationProperties applicationProperties, UsageRecorder usageRecorder,
                                           @Qualifier("requestMappingHandlerMapping") ObjectProvider<RequestMappingHandlerMapping> controllers)
        throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/api/auth/guest",
                    "/api/auth/api-token",
                    "/api/auth/google",
                    "/api/auth/google/desktop",
                    "/api/auth/google/desktop/callback",
                    "/api/auth/google/desktop/token",
                    "/api/token/refresh",
                    "/api/logout",
                    "/api/health",
                    "/error",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/webjars/**"
                ).permitAll()
                // The administration, whatever its controllers' own @PreAuthorize: a new endpoint there is closed at once
                .requestMatchers("/api/admin/**", "/api/gtfs/**", "/api/v2/me/api-tokens/**").hasRole("ADMIN")
                // Guest devices use the rest of the API, not what belongs to an account
                .requestMatchers("/api/v2/me/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                // Refused by the rules above: the same problem detail as a controller's @PreAuthorize (ApiExceptionHandler)
                .accessDeniedHandler((request, response, denied) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    response.getWriter().write("{\"title\":\"Forbidden\",\"status\":403,\"detail\":\"Access denied\"}");
                })
            )
            .cors(Customizer.withDefaults())
            // Both after the JWT authentication, in this order (filters at the same place keep the order they were added
            // in): usage counts first, so that the rate limiting's refusals are counted too
            .addFilterAfter(new UsageFilter(usageRecorder, () -> controllers.getObject().getHandlerMethods().keySet().stream()
                .flatMap(route -> route.getPatternValues().stream()).toList()), BearerTokenAuthenticationFilter.class)
            .addFilterAfter(new RateLimitFilter(rateLimiter, applicationProperties.getRateLimit()), BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = getSecretKey();
        return NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey key = getSecretKey();
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(key);
        return new NimbusJwtEncoder(jwkSource);
    }

    /** HS256 signs with 256 bits: a shorter secret could be found by trying them, and whoever has it is any admin */
    static final int MIN_SECRET_LENGTH = 32;

    private SecretKey getSecretKey() {
        if (jwtSecretKey == null || jwtSecretKey.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("security.jwt.token.secret-key must be at least " + MIN_SECRET_LENGTH
                + " random characters (openssl rand -base64 48)");
        }
        byte[] keyBytes = Base64.getEncoder().encode(jwtSecretKey.getBytes());
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("auth");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
