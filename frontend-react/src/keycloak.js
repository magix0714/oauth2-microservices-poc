import Keycloak from "keycloak-js";

export function createKeycloak() {
  return new Keycloak({
    url: "http://localhost:8081",
    realm: "demo-realm",
    clientId: "frontend-client"
  });
}
