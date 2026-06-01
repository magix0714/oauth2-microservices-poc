# Order Service Security Guide

## Purpose
`order-service` demonstrates resource-server authorization and business authorization together:
- scope checks at endpoint level
- extracting identity claims from JWT
- ownership checks in business logic

## Security Configuration
Source: `src/main/java/com/example/orderservice/config/SecurityConfig.java`

1. JWT Validation
- Configured via `spring.security.oauth2.resourceserver.jwt.issuer-uri`.
- Signature and claims are validated using Keycloak metadata/JWKS.

2. Scope Checks
- `GET /orders/**` requires `SCOPE_orders.read`
- `POST /orders` requires `SCOPE_orders.write`

3. Claims Endpoint
- `GET /orders/claims` requires authentication.
- It is used for introspection/demo of claim extraction (`sub`, `preferred_username`, `scope`, `realm_access.roles`).

4. Why Business Authorization Exists
- Endpoint scope checks only verify coarse permissions.
- Ownership checks ensure users can only access their own orders.
- `GET /orders/{id}` throws access denied if token user is not the order owner.

## Demonstration Endpoints
- `GET /orders`
  - requires `orders.read`
  - returns only authenticated user's orders

- `GET /orders/{id}`
  - requires `orders.read`
  - applies ownership check

- `POST /orders`
  - requires `orders.write`
  - creates order for the authenticated user

- `GET /orders/claims`
  - requires a valid token
  - returns selected claims to demonstrate JWT reading
