package com.example.orderservice.model;

import java.util.List;

public record JwtClaimsView(
        String subject,
        String preferredUsername,
        String issuer,
        List<String> scopes,
        List<String> roles) {
}
