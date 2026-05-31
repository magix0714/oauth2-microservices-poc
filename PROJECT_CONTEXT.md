# Create a Modern OAuth2/OIDC Microservices POC

Create a complete runnable proof-of-concept application demonstrating a modern production-style microservices architecture using:

- Java 25
- Spring Boot 4.x
- Spring Security 7.x
- Spring Cloud Gateway
- Keycloak
- React
- Maven
- Docker Compose

The purpose of this project is educational.

The code should be production-inspired, heavily documented, and demonstrate how OAuth2, OpenID Connect, JWT, API Gateway security, and service-to-service security work together.

Do not optimize for brevity.
Optimize for clarity and learning.

---

# Project Structure

Create a Maven multi-module monorepo.

oauth2-microservices-poc/

- pom.xml
- README.md
- docker-compose.yml

Modules:

- api-gateway
- product-service
- order-service
- payment-service
- frontend-react

Infrastructure:

- Keycloak container
- Optional PostgreSQL container for Keycloak
- All services wired together through Docker Compose

---

# Learning Objectives

The project must demonstrate:

1. OAuth2 Authorization Code Flow with PKCE
2. OpenID Connect login
3. JWT access tokens
4. JWT validation at API Gateway
5. JWT validation at Resource Servers
6. Roles vs Scopes
7. API Gateway authorization
8. Resource Server authorization
9. Service-to-service authentication
10. Why services still validate JWT even though the gateway validates it
11. Keycloak realm configuration
12. JWKS endpoint usage
13. Token inspection and claims

Every major security configuration must include explanatory comments.

---

# Architecture

Browser
↓
React Frontend
↓
API Gateway
↓
Product Service
Order Service
Payment Service

Keycloak
↓
OAuth2 + OIDC Provider
↓
JWT Issuer

---

# Keycloak Configuration

Create everything automatically if possible.

Realm:

demo-realm

Clients:

frontend-client
gateway-client
order-service-client

Users:

user/password

Roles:
USER

Scopes:
products.read
orders.read
orders.write

admin/password

Roles:
ADMIN

Scopes:
products.read
orders.read
orders.write
payments.write

Document exactly how Keycloak is configured.

Provide screenshots folder placeholders and instructions.

---

# React Frontend

Create a React application.

Requirements:

- Login using Keycloak
- Logout
- Display current user
- Display JWT claims
- Display roles
- Display scopes

Buttons:

- View Products
- View Orders
- Create Order
- Make Payment

Show response payloads on screen.

Show authorization failures.

Show token expiry time.

Show ID token and Access token structure.

Provide a page called:

Security Playground

that explains:

- OAuth2
- OIDC
- JWT
- Roles
- Scopes

using data from the running system.

---

# API Gateway

Use Spring Cloud Gateway.

Responsibilities:

- Validate JWT
- Route requests
- Enforce route-level authorization
- Forward JWT downstream

Routes:

/api/products/\*\*
→ product-service

/api/orders/\*\*
→ order-service

/api/payments/\*\*
→ payment-service

Authorization rules:

GET /api/products/public

Public

GET /api/products

Authenticated

GET /api/orders

Requires scope orders.read

POST /api/orders

Requires scope orders.write

POST /api/payments

Requires scope payments.write

Add extensive comments explaining:

- why gateway validation exists
- why gateway is not enough
- why resource servers still validate tokens

---

# Product Service

Spring Boot Resource Server.

Validate JWT using Keycloak JWKS.

Endpoints:

GET /products/public

No authentication

Returns public catalog

GET /products

Authenticated

Returns full catalog

POST /products

ADMIN only

Adds product

Use in-memory storage.

---

# Order Service

Spring Boot Resource Server.

Validate JWT.

Endpoints:

GET /orders

Requires orders.read

POST /orders

Requires orders.write

Extract current user from JWT.

Return orders only belonging to authenticated user.

Use in-memory storage.

Demonstrate:

- reading JWT claims
- role checks
- scope checks

---

# Payment Service

Spring Boot Resource Server.

Validate JWT.

Endpoints:

POST /payments

Requires payments.write

Simulate payment processing.

Demonstrate:

- scope authorization
- ownership validation
- business authorization

Explain the difference between:

Authentication
Authorization
Business Authorization

using actual code comments.

---

# Service-to-Service Security

Create one realistic example.

Order Service calls Product Service.

Use OAuth2 Client Credentials flow.

Do NOT bypass security.

Demonstrate:

1. Order Service obtaining token from Keycloak
2. Order Service calling Product Service
3. Product Service validating service token

Explain:

- machine identities
- service accounts
- client credentials flow

Include diagrams in README.

---

# Security Demonstrations

README must include examples:

Example 1

Anonymous user calls:

GET /api/products/public

Success

Example 2

Anonymous user calls:

GET /api/orders

401

Example 3

USER calls:

GET /api/orders

Success

Example 4

USER calls:

POST /api/payments

403

Example 5

ADMIN calls:

POST /api/payments

Success

Example 6

Tampered JWT

401

Example 7

Expired JWT

401

Explain every result.

---

# JWT Learning Section

Create documentation showing:

JWT Header

JWT Payload

JWT Signature

Example claims:

sub
preferred_username
scope
roles
exp
iat

Explain how Keycloak signs tokens.

Explain how services verify tokens.

Explain JWKS.

---

# Docker

Everything must run using:

docker-compose up

Include:

- Keycloak
- Keycloak database if needed
- Gateway
- Product Service
- Order Service
- Payment Service
- React Frontend

Provide health checks.

Provide startup order.

---

# README Requirements

README must be written as a tutorial.

Sections:

1. Architecture Overview
2. OAuth2 Fundamentals
3. OpenID Connect Fundamentals
4. JWT Fundamentals
5. Keycloak Setup
6. Running the Project
7. Testing the Flows
8. Security Walkthrough
9. Service-to-Service Authentication
10. Common Production Patterns
11. Common Mistakes
12. Troubleshooting

Include architecture diagrams using Mermaid.

---

# Coding Standards

Use:

- constructor injection
- records where appropriate
- modern Spring Security configuration
- SecurityFilterChain
- OAuth2 Resource Server
- OAuth2 Client
- no deprecated APIs

Avoid:

- WebSecurityConfigurerAdapter
- deprecated Spring Security approaches
- custom JWT parsing logic

Use official Spring Security approaches.

---

# Most Important Requirement

This is not merely a CRUD application.

The primary goal is to teach:

- OAuth2
- OIDC
- JWT
- API Gateway security
- Resource Server security
- Service-to-service authentication

through working code and extensive documentation.
