# OAuth2/OIDC Microservices POC

## 1. Architecture Overview
- React frontend authenticates users via Keycloak.
- API Gateway validates JWTs and routes requests.
- Product, Order, and Payment services validate JWTs again.
- Order Service calls Product Service via OAuth2 Client Credentials.

## 2. OAuth2 Fundamentals
_TODO: Explain Authorization Code with PKCE and Client Credentials in this project._

## 3. OpenID Connect Fundamentals
_TODO: Explain ID token usage and user identity claims._

## 4. JWT Fundamentals
_TODO: Explain token structure, signature, claims, and validation._

## 5. Keycloak Setup
_TODO: Document realm, clients, users, roles, and scopes setup._

## 6. Running the Project
```bash
docker-compose up --build
```

## 7. Testing the Flows
_TODO: Add step-by-step API and UI test flows._

## 8. Security Walkthrough
_TODO: Add gateway/resource-server validation walkthrough and expected outcomes._

## 9. Service-to-Service Authentication
_TODO: Add Order Service -> Product Service client credentials walkthrough._

## 10. Common Production Patterns
_TODO: Add production patterns relevant to this architecture._

## 11. Common Mistakes
_TODO: Add common OAuth2/OIDC/JWT implementation pitfalls._

## 12. Troubleshooting
_TODO: Add startup/auth/token validation troubleshooting steps._

## Repository Structure
```text
oauth2-microservices-poc/
├── pom.xml
├── README.md
├── docker-compose.yml
├── ARCHITECTURE.md
├── SECURITY.md
├── PROJECT_CONTEXT.md
├── api-gateway/
│   └── pom.xml
├── product-service/
│   └── pom.xml
├── order-service/
│   └── pom.xml
├── payment-service/
│   └── pom.xml
├── frontend-react/
└── keycloak/
```
