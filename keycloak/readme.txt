http://localhost:8081/
admin:admin as specified in docker-compose.yml

please share the CURL commands you used to test.
10:40 AM

curl -s -o /tmp/u.json -w '%{http_code}\n' \
  -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&client_id=frontend-client&username=user&password=password&scope=openid%20orders.read%20orders.write%20products.read'
curl -s -o /tmp/a.json -w '%{http_code}\n' \
  -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&client_id=frontend-client&username=admin&password=password&scope=openid%20orders.read%20orders.write%20products.read%20payments.write'
curl -s -o /tmp/s.json -w '%{http_code}\n' \
  -X POST 'http://localhost:8081/realms/demo-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials&client_id=order-service-client&client_secret=order-service-secret'
# Optional: inspect scopes in returned tokens
for f in /tmp/u.json /tmp/a.json /tmp/s.json; do
  echo "--- $f"
  sed -n '1p' "$f" | rg -o '"scope":"[^"]*"'
done

Output:
--- /tmp/u.json
"scope":"openid products.read orders.write orders.read"
--- /tmp/a.json
"scope":"openid products.read orders.write payments.write orders.read"
--- /tmp/s.json
"scope":"products.read"
