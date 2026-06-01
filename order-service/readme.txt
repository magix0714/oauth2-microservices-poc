Prompt:
Implement Order Service.

Demonstrate:

- extracting claims
- scope checks
- ownership checks
- reading JWT claims


Output:
Verification completed on order-service at http://localhost:8084 with these observed results:

anonymous GET /orders -> 401
user GET /orders -> 200
user GET /orders/claims -> 200
user creates order POST /orders -> 201
user reads own order GET /orders/{id} -> 200
admin reads user’s order GET /orders/{id} -> 403
admin GET /orders -> 200
Use these exact curl commands:

# 1) Get USER token
USER_TOKEN=$(
  curl -s -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -d 'grant_type=password&client_id=frontend-client&username=user&password=password&scope=openid%20orders.read%20orders.write%20products.read' \
  | jq -r .access_token
)
# 2) Get ADMIN token
ADMIN_TOKEN=$(
  curl -s -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -d 'grant_type=password&client_id=frontend-client&username=admin&password=password&scope=openid%20orders.read%20orders.write%20products.read%20payments.write' \
  | jq -r .access_token
)
# 3) Anonymous should fail (401)
curl -i http://localhost:8084/orders
# 4) User reads own order list (scope check orders.read) -> 200
curl -i -H "Authorization: Bearer $USER_TOKEN" \
  http://localhost:8084/orders
# 5) Read JWT claims (claim extraction demo) -> 200
curl -i -H "Authorization: Bearer $USER_TOKEN" \
  http://localhost:8084/orders/claims
# 6) User creates order (scope check orders.write) -> 201
CREATE_RESP=$(
  curl -s -X POST http://localhost:8084/orders \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $USER_TOKEN" \
    -d '{"productName":"Coffee Beans","quantity":3}'
)
echo "$CREATE_RESP"
ORDER_ID=$(echo "$CREATE_RESP" | jq -r .id)
echo "ORDER_ID=$ORDER_ID"
# 7) User reads own created order (ownership check) -> 200
curl -i -H "Authorization: Bearer $USER_TOKEN" \
  "http://localhost:8084/orders/$ORDER_ID"
# 8) Admin tries to read user's order (ownership check) -> 403
curl -i -H "Authorization: Bearer $ADMIN_TOKEN" \
  "http://localhost:8084/orders/$ORDER_ID"
# 9) Admin reads own order list -> 200
curl -i -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8084/orders
