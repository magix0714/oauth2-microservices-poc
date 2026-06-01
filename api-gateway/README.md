# API Gateway Documentation

## What This Module Does
The API Gateway is the public security entrypoint for API traffic. It performs four jobs before traffic reaches any backend service:
- validates JWT access tokens
- enforces route-level authorization rules
- enforces role/scope policies at the edge
- forwards authorized traffic to downstream services

## Why Gateway Validation Exists
JWT validation at the gateway provides a consistent first-line defense:
- blocks invalid or expired tokens before internal services are reached
- centralizes route-level policies so each endpoint path has explicit security intent
- reduces accidental exposure if downstream services are not yet hardened

Gateway validation is necessary but not sufficient. Resource services must still validate JWTs and apply domain/business authorization.

## Security Configuration Walkthrough
Source: `src/main/java/com/example/gateway/config/SecurityConfig.java`

Security rules are evaluated in path order:
- `/actuator/health` and `/actuator/info` are public for operational checks
- `/api/products/public` is public
- `GET /api/products/**` requires authentication
- `POST /api/products/**` requires `ROLE_ADMIN`
- `GET /api/orders/**` requires `SCOPE_orders.read`
- `POST /api/orders/**` requires `SCOPE_orders.write`
- `POST /api/payments/**` requires `SCOPE_payments.write`
- `/api/admin/**` requires `ROLE_ADMIN`
- any other route requires authentication

JWT resource server mode:
- the gateway uses Keycloak as issuer (`issuer-uri` in `application.yml`)
- Spring validates signature, issuer, and expiration
- custom authority extraction maps token content into Spring authorities

Authority mapping behavior:
- `scope` claim values become `SCOPE_*` authorities
- `realm_access.roles` values become `ROLE_*` authorities

This mapping lets us enforce both scopes and roles at the gateway without custom token parsing libraries.

## Route Configuration Walkthrough
Source: `src/main/resources/application.yml`

Configured routes:
- `/api/products/**` -> `http://localhost:8082` with prefix stripped
- `/api/orders/**` -> `http://localhost:8083` with prefix stripped
- `/api/payments/**` -> `http://localhost:8084` with prefix stripped
- `/api/admin/products/**` -> `http://localhost:8082` using rewrite for admin namespace

Current route URIs are placeholders to allow gateway security verification before backend services exist.

## Expected Gateway Outcomes
Before backend services exist, security outcomes can still be verified:
- no token to protected endpoint -> `401 Unauthorized`
- wrong scope to endpoint -> `403 Forbidden`
- valid token + allowed scope/role -> gateway passes auth checks and then downstream will likely return `5xx` because service is not running

## Verification Commands
1. Start Keycloak and gateway.
2. Obtain tokens from Keycloak (`user`, `admin`).
3. Call gateway routes and observe 401/403/security behavior.

This verifies security enforcement independently from business endpoints.
