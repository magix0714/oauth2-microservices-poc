import { useEffect, useMemo, useState } from "react";
import { createKeycloak } from "./keycloak";

const gatewayBase = "http://localhost:8079";

function parseJwt(token) {
  if (!token) return null;
  const payload = token.split(".")[1];
  if (!payload) return null;
  const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
  const decoded = atob(normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), "="));
  return JSON.parse(decoded);
}

function formatTimestamp(seconds) {
  if (!seconds) return "N/A";
  return new Date(seconds * 1000).toLocaleString();
}

async function apiCall(path, token, method = "GET", body) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers.Authorization = `Bearer ${token}`;
  const response = await fetch(`${gatewayBase}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });
  const text = await response.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }
  return { status: response.status, data };
}

export default function App() {
  const [ready, setReady] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [accessToken, setAccessToken] = useState(null);
  const [idToken, setIdToken] = useState(null);
  const [result, setResult] = useState(null);
  const [orderIdInput, setOrderIdInput] = useState("");
  const [amountInput, setAmountInput] = useState("990");
  const [newProductName, setNewProductName] = useState("UI Demo Product");
  const [newProductDescription, setNewProductDescription] = useState("Created from API Playground");
  const [newProductPrice, setNewProductPrice] = useState("19.99");
  const [newProductPublic, setNewProductPublic] = useState(false);
  const [keycloakClient, setKeycloakClient] = useState(null);
  const [initError, setInitError] = useState("");

  useEffect(() => {
    const kc = createKeycloak();
    setKeycloakClient(kc);
    kc.init({
      onLoad: "check-sso",
      pkceMethod: "S256",
      checkLoginIframe: false,
      silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
      silentCheckSsoFallback: false
    })
      .then((isAuthenticated) => {
        setAuthenticated(isAuthenticated);
        setAccessToken(kc.token || null);
        setIdToken(kc.idToken || null);
        setReady(true);
      })
      .catch((error) => {
        setInitError(String(error));
        setReady(true);
      });
  }, []);

  useEffect(() => {
    if (!authenticated || !keycloakClient) return undefined;
    const timer = setInterval(async () => {
      try {
        const refreshed = await keycloakClient.updateToken(30);
        if (refreshed) {
          setAccessToken(keycloakClient.token || null);
          setIdToken(keycloakClient.idToken || null);
        }
      } catch {
        setAuthenticated(false);
        setAccessToken(null);
        setIdToken(null);
      }
    }, 10000);
    return () => clearInterval(timer);
  }, [authenticated, keycloakClient]);

  const accessClaims = useMemo(() => parseJwt(accessToken), [accessToken]);
  const idClaims = useMemo(() => parseJwt(idToken), [idToken]);
  const principalName =
    accessClaims?.preferred_username ||
    idClaims?.preferred_username ||
    accessClaims?.sub ||
    idClaims?.sub ||
    "N/A";
  const scopes = accessClaims?.scope ? accessClaims.scope.split(" ") : [];
  const roles = accessClaims?.realm_access?.roles || [];

  const runCall = async (label, fn) => {
    try {
      const output = await fn();
      setResult({ label, ...output });
    } catch (error) {
      setResult({ label, status: "error", data: String(error) });
    }
  };

  if (!ready) {
    return <main className="shell">Loading authentication bootstrap...</main>;
  }

  return (
    <main className="shell">
      <header className="hero">
        <h1>OAuth2 Microservices UI</h1>
        <p>Keycloak login, JWT claim visibility, API exercises, and security learning in one place.</p>
        <div className="actions">
          {!authenticated ? (
            <button onClick={() => keycloakClient && keycloakClient.login()}>Login with Keycloak</button>
          ) : (
            <button
              onClick={() => {
                if (keycloakClient) {
                  keycloakClient.logout({ redirectUri: window.location.origin });
                }
              }}
            >
              Logout
            </button>
          )}
        </div>
      </header>

      {initError && (
        <section className="panel">
          <h2>Keycloak Init Error</h2>
          <pre>{initError}</pre>
        </section>
      )}

      <section className="panel">
        <h2>Session</h2>
        <p>Status: {authenticated ? "Authenticated" : "Anonymous"}</p>
        <p>User: {idClaims?.preferred_username || "N/A"}</p>
        <p>Principal: {principalName}</p>
        <p>Access token expiry: {formatTimestamp(accessClaims?.exp)}</p>
        <p>ID token expiry: {formatTimestamp(idClaims?.exp)}</p>
      </section>

      <section className="panel">
        <h2>Claims</h2>
        <p>Scopes: {scopes.length ? scopes.join(", ") : "None"}</p>
        <p>Roles: {roles.length ? roles.join(", ") : "None"}</p>
        <details>
          <summary>Access Token Claims</summary>
          <pre>{JSON.stringify(accessClaims, null, 2)}</pre>
        </details>
        <details>
          <summary>ID Token Claims</summary>
          <pre>{JSON.stringify(idClaims, null, 2)}</pre>
        </details>
      </section>

      <section className="panel">
        <h2>API Playground</h2>
        <div className="grid">
          <button onClick={() => runCall("GET /api/products/public", () => apiCall("/api/products/public", accessToken))}>
            View Public Products
          </button>
          <button onClick={() => runCall("GET /api/products", () => apiCall("/api/products", accessToken))}>
            View Products (Auth)
          </button>
          <button onClick={() => runCall("GET /api/orders", () => apiCall("/api/orders", accessToken))}>
            View Orders
          </button>
          <button
            onClick={() =>
              runCall("POST /api/orders", () =>
                apiCall("/api/orders", accessToken, "POST", { productName: "UI Order", quantity: 1 })
              )
            }
          >
            Create Order
          </button>
          <button
            onClick={() => runCall("GET /api/orders/service-products", () => apiCall("/api/orders/service-products", accessToken))}
          >
            Service-to-Service Demo
          </button>
        </div>
        <div className="adminBox">
          <h3>Admin Product API</h3>
          <p>Requires `ROLE_ADMIN` (or configured admin authority in gateway/product service).</p>
          {!roles.includes("ADMIN") && (
            <p>
              ADMIN hint: your current token does not include <code>ROLE_ADMIN</code>, so this call will return{" "}
              <code>403 Forbidden</code>. Log in as <code>admin</code> to test product creation.
            </p>
          )}
          <label>
            Name
            <input value={newProductName} onChange={(e) => setNewProductName(e.target.value)} placeholder="Product name" />
          </label>
          <label>
            Description
            <input
              value={newProductDescription}
              onChange={(e) => setNewProductDescription(e.target.value)}
              placeholder="Product description"
            />
          </label>
          <label>
            Price
            <input value={newProductPrice} onChange={(e) => setNewProductPrice(e.target.value)} placeholder="19.99" />
          </label>
          <label className="checkboxLabel">
            <input type="checkbox" checked={newProductPublic} onChange={(e) => setNewProductPublic(e.target.checked)} />
            Public product
          </label>
          <button
            onClick={() =>
              runCall("POST /api/admin/products", () =>
                apiCall("/api/admin/products", accessToken, "POST", {
                  name: newProductName,
                  description: newProductDescription,
                  price: newProductPrice,
                  public: newProductPublic
                })
              )
            }
          >
            Add Product (Admin)
          </button>
        </div>
        <div className="paymentBox">
          <label>
            Order ID
            <input value={orderIdInput} onChange={(e) => setOrderIdInput(e.target.value)} placeholder="e.g. 2005" />
          </label>
          <label>
            Amount (cents)
            <input value={amountInput} onChange={(e) => setAmountInput(e.target.value)} placeholder="990" />
          </label>
          <button
            onClick={() =>
              runCall("POST /api/payments", () =>
                apiCall("/api/payments", accessToken, "POST", {
                  orderId: Number(orderIdInput),
                  amountCents: Number(amountInput)
                })
              )
            }
          >
            Make Payment
          </button>
        </div>
        {result && (
          <div className="result">
            <h3>{result.label}</h3>
            <p>Status: {result.status}</p>
            <pre>{JSON.stringify(result.data, null, 2)}</pre>
          </div>
        )}
      </section>

      <section className="panel playground">
        <h2>Security Playground</h2>
        <p>
          OAuth2: User login uses Authorization Code + PKCE. Service-to-service calls use Client Credentials.
        </p>
        <p>OIDC: Identity claims are shown from ID token (`preferred_username`, `sub`, expiry).</p>
        <p>JWT: Access token claims drive API authorization through scopes and roles.</p>
        <p>Roles: {roles.length ? roles.join(", ") : "No role claims present in current token."}</p>
        <p>Scopes: {scopes.length ? scopes.join(", ") : "No scopes present."}</p>
      </section>
    </main>
  );
}
