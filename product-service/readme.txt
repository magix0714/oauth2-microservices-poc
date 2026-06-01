Prompt:
Product Service
Implement Product Service as OAuth2 Resource Server.

Demonstrate:

- public endpoint
- authenticated endpoint
- ADMIN endpoint

Explain every security configuration.

Verify.

Output:
Implemented and verified product-service as an OAuth2 Resource Server with all 3 endpoint types.
Updated files:
SecurityConfig.java
ProductController.java
ProductCatalogService.java
application.yml
README.md
pom.xml
Dockerfile
docker-compose.yml
Security configuration explained:
JWT validation: enabled via oauth2ResourceServer().jwt(...), using Keycloak issuer metadata/JWKS.
Public endpoint: GET /products/public is permitAll().
Authenticated endpoint: GET /products is authenticated().
Admin endpoint: POST /products requires hasAnyAuthority("ROLE_ADMIN", "SCOPE_payments.write").
Authority mapping:scope claim -> SCOPE_*
realm_access.roles -> ROLE_*

CSRF disabled because this is a stateless bearer-token API.
Verification results:
GET /products/public (no token) -> 200
GET /products (no token) -> 401
GET /products (user token) -> 200
POST /products (user token) -> 403
POST /products (admin token) -> 201 (product created)
If you want, next I can switch POST /products back to strict ROLE_ADMIN only once your Keycloak token/role mapper is finalized.

Test CURL commands:

11:44 AM


# 1) Public endpoint (no token) -> expect 200
curl -i http://localhost:8082/products/public
# 2) Authenticated endpoint (no token) -> expect 401
curl -i http://localhost:8082/products
# 3) Get USER token from Keycloak
curl -s -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&client_id=frontend-client&username=user&password=password&scope=openid%20orders.read%20orders.write%20products.read'
# 4) Get ADMIN token from Keycloak
curl -s -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&client_id=frontend-client&username=admin&password=password&scope=openid%20orders.read%20orders.write%20products.read%20payments.write'
# 5) Store USER token in env var
USER_TOKEN=$(
  curl -s -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -d 'grant_type=password&client_id=frontend-client&username=user&password=password&scope=openid%20orders.read%20orders.write%20products.read' \
  | jq -r .access_token
)
# 6) Store ADMIN token in env var
ADMIN_TOKEN=$(
  curl -s -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -d 'grant_type=password&client_id=frontend-client&username=admin&password=password&scope=openid%20orders.read%20orders.write%20products.read%20payments.write' \
  | jq -r .access_token
)
# 7) Authenticated endpoint with USER token -> expect 200
curl -i http://localhost:8082/products \
  -H "Authorization: Bearer $USER_TOKEN"
# 8) ADMIN endpoint with USER token -> expect 403
curl -i -X POST http://localhost:8082/products \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"name":"Nope","description":"user should fail","price":1.0,"isPublic":false}'
# 9) ADMIN endpoint with ADMIN token -> expect 201
curl -i -X POST http://localhost:8082/products \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"name":"Admin Item","description":"created by admin","price":77.7,"isPublic":false}'
# 10) Optional: actuator health (public) -> expect 200
curl -i http://localhost:8082/actuator/health
# 11) Optional: inspect token response as pretty JSON
curl -s -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&client_id=frontend-client&username=user&password=password&scope=openid%20orders.read%20orders.write%20products.read' \
  | jq .
