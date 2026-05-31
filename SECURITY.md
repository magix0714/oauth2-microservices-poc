# Security

## Security Objectives
This project teaches modern security patterns for microservices using OAuth2, OIDC, and JWT with layered enforcement at gateway and resource services.

Primary goals:
- OAuth2 Authorization Code + PKCE for user login
- OIDC identity handling
- JWT-based API authorization
- route-level authorization at API Gateway
- endpoint and business authorization in services
- service-to-service authentication with Client Credentials

## Security Components
- Keycloak: OAuth2 Authorization Server and OIDC Provider
- React Frontend: OAuth2/OIDC public client
- API Gateway: JWT validation and edge authorization
- Product/Order/Payment Services: OAuth2 Resource Servers with independent JWT validation

## Keycloak Security Model
Realm:
- `demo-realm`

Clients:
- `frontend-client` for browser login
- `gateway-client` for gateway integration
- `order-service-client` for service account and client credentials

Users and roles:
- `user/password` -> `USER`
- `admin/password` -> `ADMIN`

Scopes used by APIs:
- `products.read`
- `orders.read`
- `orders.write`
- `payments.write`

## User Authentication Flow
1. User starts login in React frontend.
2. Frontend performs Authorization Code Flow with PKCE against Keycloak.
3. Keycloak returns tokens to frontend after successful authentication.
4. Frontend calls gateway with bearer access token.

OIDC outcomes:
- ID token provides identity claims.
- Access token authorizes API operations.

## Token Validation Strategy
JWT validation happens at two layers:
- API Gateway validates each inbound token.
- Each backend resource service validates token again.

Why both layers exist:
- Gateway enforces consistent edge policy.
- Services remain secure if reached through non-gateway paths or during misrouting.
- Service-level checks need local context (ownership and business rules) unavailable at edge.

Verification mechanism:
- Tokens are signed by Keycloak.
- Gateway and services verify signature and token integrity via Keycloak JWKS.
- Expired, malformed, or tampered tokens are rejected with `401`.

## Authorization Model
### Gateway authorization
- `GET /api/products/public` -> public
- `GET /api/products` -> authenticated
- `GET /api/orders` -> scope `orders.read`
- `POST /api/orders` -> scope `orders.write`
- `POST /api/payments` -> scope `payments.write`

### Resource service authorization
- Product Service:
  - `GET /products/public` public
  - `GET /products` authenticated
  - `POST /products` admin-only
- Order Service:
  - `GET /orders` requires `orders.read`
  - `POST /orders` requires `orders.write`
  - return only orders owned by authenticated subject
- Payment Service:
  - `POST /payments` requires `payments.write`
  - applies business authorization checks beyond token scopes

## Service-to-Service Security
Use Client Credentials for machine identity:
1. Order Service authenticates as `order-service-client`.
2. Order Service requests an access token from Keycloak.
3. Order Service calls Product Service using that token.
4. Product Service validates token as a normal resource server.

Concepts demonstrated:
- machine identities
- service accounts
- delegated service authorization independent of end-user token

## Expected Security Demonstrations
Required outcomes:
1. Anonymous `GET /api/products/public` -> success
2. Anonymous `GET /api/orders` -> `401`
3. USER `GET /api/orders` -> success
4. USER `POST /api/payments` -> `403`
5. ADMIN `POST /api/payments` -> success
6. Tampered JWT -> `401`
7. Expired JWT -> `401`

## JWT Learning Checklist
Document and demonstrate:
- JWT header
- JWT payload
- JWT signature
- claims: `sub`, `preferred_username`, `scope`, `roles`, `exp`, `iat`
- how Keycloak signs tokens
- how gateway/services verify tokens with JWKS

## Security Implementation Standards
Required implementation style:
- modern `SecurityFilterChain` configuration
- OAuth2 Resource Server support
- OAuth2 Client support for client credentials
- constructor injection
- no deprecated Spring Security approaches

Explicitly avoid:
- `WebSecurityConfigurerAdapter`
- deprecated Spring Security APIs
- custom token parsing where framework support exists
