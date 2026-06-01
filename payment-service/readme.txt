
Prompt:
Payment Service
Implement Payment Service.

Demonstrate:

- payments.write scope
- business authorization
- ownership validation

Verify.


Output:
I'll implement payment-service as a resource server with payments.write scope enforcement plus order ownership/business checks, then I'll run curl verification and share results.

I confirmed payment-service is still a skeleton, so I'm creating it on port 8085 and wiring it to call order-service for ownership validation before processing payments.

I've added security scope enforcement; next I'm adding payment business rules and the ownership check against order-service using the caller's bearer token.

Implemented payment-service with scope enforcement, business authorization, and ownership validation.
Updated files:
payment-service/pom.xml
PaymentServiceApplication.java
SecurityConfig.java
PaymentController.java
PaymentService.java
OrderOwnershipClient.java
PaymentRequest.java
PaymentResult.java
application.yml
Dockerfile
docker-compose.yml
What it demonstrates:
payments.write scope: POST /payments requires SCOPE_payments.write.
Ownership validation: payment service calls order-service /orders/{id} with caller's token.403 from order-service -> payer does not own that order.
404 -> order does not exist.

Business authorization:blocks duplicate payment for the same order (409 Conflict)
validates positive payment amount (400 Bad Request)

Verification results:
user token paying own order (no payments.write) -> 403
admin token paying user order -> 403
admin token paying admin-owned order -> 201
repeat payment for same order -> 409
Observed run:
user_pay_without_scope=403
admin_pays_user_order=403
admin_pays_own_order=201
admin_duplicate_payment=409


Use these for the new payment-service (http://localhost:8085):

# 1) Get USER token (no payments.write)
USER_TOKEN=$(
  curl -s -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -d 'grant_type=password&client_id=frontend-client&username=user&password=password&scope=openid%20orders.read%20orders.write%20products.read' \
  | jq -r .access_token
)

# 2) Get ADMIN token (includes payments.write)
ADMIN_TOKEN=$(
  curl -s -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -d 'grant_type=password&client_id=frontend-client&username=admin&password=password&scope=openid%20orders.read%20orders.write%20products.read%20payments.write' \
  | jq -r .access_token
)
# 3) Create one order as USER (for ownership tests)
USER_ORDER_ID=$(
  curl -s -X POST http://localhost:8084/orders \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $USER_TOKEN" \
    -d '{"productName":"User Order","quantity":1}' \
  | jq -r .id
)

# 4) Create one order as ADMIN
ADMIN_ORDER_ID=$(
  curl -s -X POST http://localhost:8084/orders \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"productName":"Admin Order","quantity":1}' \
  | jq -r .id
)
# 5) Scope check: USER tries payment (expect 403)
curl -i -X POST http://localhost:8085/payments \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d "{\"orderId\":$USER_ORDER_ID,\"amountCents\":990}"
# 6) Ownership check: ADMIN tries to pay USER order (expect 403)
curl -i -X POST http://localhost:8085/payments \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"orderId\":$USER_ORDER_ID,\"amountCents\":990}"
# 7) Valid payment: ADMIN pays ADMIN order (expect 201)
curl -i -X POST http://localhost:8085/payments \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"orderId\":$ADMIN_ORDER_ID,\"amountCents\":14900}"
# 8) Business rule: duplicate payment (expect 409)
curl -i -X POST http://localhost:8085/payments \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"orderId\":$ADMIN_ORDER_ID,\"amountCents\":14900}"
