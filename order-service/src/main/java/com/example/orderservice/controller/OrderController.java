package com.example.orderservice.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.orderservice.model.CreateOrderRequest;
import com.example.orderservice.model.ServiceToServiceCallResult;
import com.example.orderservice.model.JwtClaimsView;
import com.example.orderservice.model.OrderRecord;
import com.example.orderservice.service.InMemoryOrderService;
import com.example.orderservice.service.ProductServiceClient;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final InMemoryOrderService orderService;
    private final ProductServiceClient productServiceClient;

    public OrderController(InMemoryOrderService orderService, ProductServiceClient productServiceClient) {
        this.orderService = orderService;
        this.productServiceClient = productServiceClient;
    }

    @GetMapping
    public List<OrderRecord> myOrders(JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();
        String userKey = extractUserKey(jwt, authentication);
        return orderService.listOwnedOrders(userKey);
    }

    @GetMapping("/{id}")
    public OrderRecord getOwnedOrder(@PathVariable("id") long id, JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();
        String userKey = extractUserKey(jwt, authentication);
        return orderService.getOwnedOrder(id, userKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderRecord createOrder(@Valid @RequestBody CreateOrderRequest request, JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();
        String userKey = extractUserKey(jwt, authentication);
        return orderService.createOrder(request, userKey);
    }

    @GetMapping("/claims")
    public JwtClaimsView claims(JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();
        String scope = jwt.getClaimAsString("scope");
        List<String> scopes = scope == null || scope.isBlank() ? List.of() : List.of(scope.split("\\s+"));

        List<String> roles = List.of();
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> mappedRoles) {
            roles = mappedRoles.stream().map(String::valueOf).toList();
        }

        return new JwtClaimsView(
                jwt.getSubject(),
                extractUserKey(jwt, authentication),
                String.valueOf(jwt.getIssuer()),
                scopes,
                roles);
    }

    @GetMapping("/service-products")
    public ServiceToServiceCallResult serviceToServiceProducts() {
        return productServiceClient.fetchProductsUsingClientCredentials();
    }

    private String extractUserKey(Jwt jwt, JwtAuthenticationToken authentication) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank() && !"null".equalsIgnoreCase(preferredUsername)) {
            return preferredUsername;
        }

        String subject = jwt.getSubject();
        if (subject != null && !subject.isBlank() && !"null".equalsIgnoreCase(subject)) {
            return subject;
        }

        String sessionState = jwt.getClaimAsString("sid");
        if (sessionState != null && !sessionState.isBlank()) {
            return sessionState;
        }

        String clientId = jwt.getClaimAsString("azp");
        if (clientId != null && !clientId.isBlank()) {
            return clientId;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt principalJwt) {
            String principalSubject = principalJwt.getSubject();
            if (principalSubject != null && !principalSubject.isBlank()) {
                return principalSubject;
            }
            String principalSid = principalJwt.getClaimAsString("sid");
            if (principalSid != null && !principalSid.isBlank()) {
                return principalSid;
            }
        }

        String name = authentication.getName();
        if (name != null && !name.isBlank() && !name.startsWith("org.springframework.security.oauth2.jwt.Jwt@")) {
            return name;
        }
        return "unknown-user";
    }
}
