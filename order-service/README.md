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

## Service-to-Service Security (Implemented)
Order Service now also acts as an OAuth2 Client for machine-to-machine calls.

### What Was Implemented
1. Service account client in Keycloak
- Client: `order-service-client`
- Grant type: `client_credentials`
- Scope used: `products.read`

2. OAuth2 client registration in Order Service
- File: `src/main/resources/application.yml`
- Configures:
  - token endpoint
  - client id/secret
  - `client_credentials` grant
  - scope for downstream product access

3. Token acquisition in code
- File: `src/main/java/com/example/orderservice/config/OAuth2ClientConfig.java`
- Uses `OAuth2AuthorizedClientManager` with `ClientCredentialsOAuth2AuthorizedClientProvider`.

4. Downstream call with bearer token
- File: `src/main/java/com/example/orderservice/service/ProductServiceClient.java`
- Acquires service token from Keycloak and calls `GET /products` on Product Service with `Authorization: Bearer <service-token>`.

5. Demo endpoint
- File: `src/main/java/com/example/orderservice/controller/OrderController.java`
- Endpoint: `GET /orders/service-products`
- Behavior:
  - validates incoming user JWT at Order Service
  - performs client-credentials flow to get service token
  - calls Product Service with service token
  - returns downstream products + diagnostic fields (`clientId`, `grantType`, `tokenType`)

### Why This Matters
- Demonstrates that user auth and service auth are separate concerns.
- Keeps inter-service trust explicit (no token sharing hacks).
- Proves Product Service independently validates the service JWT.

### Quick Verification
1. Get user token:
```bash
USER_TOKEN=$(
  curl -s -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -d 'grant_type=password&client_id=frontend-client&username=user&password=password&scope=openid%20orders.read%20orders.write%20products.read' \
  | jq -r .access_token
)
```

2. Call service-to-service endpoint:
```bash
curl -i -H "Authorization: Bearer $USER_TOKEN" \
  http://localhost:8084/orders/service-products
```
Expected: `200` with payload containing:
- `"clientId":"order-service-client"`
- `"grantType":"client_credentials"`
- `"tokenType":"Bearer"`
- downstream products from Product Service.
