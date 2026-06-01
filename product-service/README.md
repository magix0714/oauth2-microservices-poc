# Product Service Security Guide

## Purpose
`product-service` is an OAuth2 Resource Server that demonstrates three endpoint trust levels:
- public endpoint (`GET /products/public`)
- authenticated endpoint (`GET /products`)
- administrator endpoint (`POST /products`)

## Security Configuration Explained
Source: `src/main/java/com/example/productservice/config/SecurityConfig.java`

1. `SecurityFilterChain`
- Disables CSRF because this API is stateless and uses bearer tokens, not browser sessions.
- Defines endpoint access rules:
  - `GET /products/public` -> `permitAll()`
  - `GET /products` -> `authenticated()`
  - `POST /products` -> `hasAnyAuthority("ROLE_ADMIN", "SCOPE_payments.write")`
- Enables OAuth2 Resource Server JWT support.

2. JWT validation
- `issuer-uri` points to Keycloak realm metadata.
- Spring Security fetches JWKS from Keycloak and verifies token signature/issuer/expiry.
- Invalid, expired, or tampered tokens are rejected before controller execution.

3. Authority mapping
- `scope` claim values are mapped to `SCOPE_*` authorities.
- `realm_access.roles` are mapped to `ROLE_*` authorities.
- This allows role checks (e.g., `ROLE_ADMIN`) to work directly against Keycloak-issued JWTs.

## Endpoint Behavior
- `GET /products/public`
  - no token required
  - returns only public catalog entries

- `GET /products`
  - valid access token required
  - returns complete in-memory catalog

- `POST /products`
  - valid access token required
  - requires admin-level authorization (`ROLE_ADMIN` or `SCOPE_payments.write` in this POC realm setup)
  - creates product in in-memory catalog and returns `201 Created`

## Why Service-Level Validation Still Matters
Even if an API gateway already validates JWTs, this service validates again to preserve defense-in-depth:
- direct/internal calls still require valid tokens
- service-specific authorization (admin-only write) is enforced where business logic lives
- accidental gateway misconfiguration does not expose service endpoints
