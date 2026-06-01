Prompt:
Service-to-Service Security

This is the most valuable learning piece.

Implement Client Credentials Flow.

Order Service must call Product Service.

Requirements:

- service account
- OAuth2 client
- token acquisition
- token validation

Explain every step.

Output:
Great focus. I'm going to wire order-service as an OAuth2 client (client-credentials), add a secure downstream call to product-service, and then verify the full token acquisition + validation path end-to-end.
I'm adding a dedicated OAuth2 client config plus a downstream client service so Order Service can acquire a client-credentials token and call Product Service with it.

Work done on Product-Service and Order-Service

Implemented. order-service now performs Client Credentials Flow to call product-service.
What was implemented:

Service account + OAuth2 client
Uses Keycloak client order-service-client (service accounts enabled).
Added Spring OAuth2 client config with client_credentials grant and products.read scope.
Token acquisition
ProductServiceClient uses OAuth2AuthorizedClientManager with ClientCredentialsOAuth2AuthorizedClientProvider.
It requests a machine token from Keycloak token endpoint.
Service-to-service call
New endpoint: GET /orders/service-products.
Internally gets client-credentials token and calls GET /products on product-service with Authorization: Bearer <service-token>.
Token validation
Product Service already validates JWT as Resource Server.
If token is missing/invalid/wrong scope, Product Service rejects; with valid products.read token, call succeeds.
Verification:

Authenticated call worked:
GET /orders/service-products -> 200
Response included:
"clientId":"order-service-client"
"grantType":"client_credentials"
"tokenType":"Bearer"
products returned from Product Service
Unauthenticated call:
GET /orders/service-products -> 401
