package com.example.productservice.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // This service is stateless and token-driven, so CSRF protection for browser sessions is disabled.
        http.csrf(csrf -> csrf.disable());

        // Endpoint-level policy:
        // - GET /products/public: no token required
        // - GET /products: authenticated token required
        // - POST /products: admin-level authorization required
        http.authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/products/public").permitAll()
                .requestMatchers(HttpMethod.GET, "/products").authenticated()
                // For this POC, admin write access accepts either:
                // - ROLE_ADMIN (role-based authorization), or
                // - SCOPE_payments.write (admin-only scope in the seeded realm)
                .requestMatchers(HttpMethod.POST, "/products").hasAnyAuthority("ROLE_ADMIN", "SCOPE_payments.write")
                .anyRequest().authenticated());

        // The service validates JWT signatures and claims using Keycloak issuer/JWKS metadata.
        // A custom converter is used to map Keycloak roles/scopes into Spring authorities.
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();

        String scope = jwt.getClaimAsString("scope");
        if (scope != null && !scope.isBlank()) {
            for (String value : scope.split("\\s+")) {
                if (!value.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + value));
                }
            }
        }

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
            for (Object role : roles) {
                String roleName = String.valueOf(role).trim();
                if (!roleName.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                }
            }
        }

        return authorities;
    }
}
