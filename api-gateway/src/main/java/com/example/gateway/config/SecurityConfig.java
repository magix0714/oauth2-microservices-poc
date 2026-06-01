package com.example.gateway.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(authz -> authz
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/api/products/public").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/products", "/api/products/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/orders/**").hasAuthority("SCOPE_orders.read")
                        .pathMatchers(HttpMethod.POST, "/api/orders/**").hasAuthority("SCOPE_orders.write")
                        .pathMatchers(HttpMethod.POST, "/api/payments/**").hasAuthority("SCOPE_payments.write")
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> Mono.just(new JwtAuthenticationToken(jwt, extractAuthorities(jwt)));
    }

    private Collection<org.springframework.security.core.GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();

        String scope = jwt.getClaimAsString("scope");
        if (scope != null && !scope.isBlank()) {
            for (String value : scope.split("\\s+")) {
                if (!value.isBlank()) {
                    authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("SCOPE_" + value));
                }
            }
        }

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
            for (Object role : roles) {
                String roleName = String.valueOf(role).trim();
                if (!roleName.isBlank()) {
                    authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + roleName));
                }
            }
        }

        return authorities;
    }
}
