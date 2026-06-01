package com.example.orderservice.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.example.orderservice.model.DownstreamProductView;
import com.example.orderservice.model.ServiceToServiceCallResult;

@Service
public class ProductServiceClient {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final AnonymousAuthenticationToken servicePrincipal;
    private final RestClient restClient;
    private final String clientRegistrationId = "order-service-client";
    private final String productEndpoint;

    public ProductServiceClient(
            OAuth2AuthorizedClientManager authorizedClientManager,
            AnonymousAuthenticationToken servicePrincipal,
            @Value("${services.product-service-base-url:http://localhost:8082}") String productServiceBaseUrl) {
        this.authorizedClientManager = authorizedClientManager;
        this.servicePrincipal = servicePrincipal;
        this.restClient = RestClient.builder().baseUrl(productServiceBaseUrl).build();
        this.productEndpoint = productServiceBaseUrl + "/products";
    }

    public ServiceToServiceCallResult fetchProductsUsingClientCredentials() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId)
                .principal(servicePrincipal)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to acquire client credentials token");
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        DownstreamProductView[] response = restClient.get()
                .uri("/products")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DownstreamProductView[].class);

        List<DownstreamProductView> products = response == null ? List.of() : Arrays.asList(response);

        return new ServiceToServiceCallResult(
                authorizedClient.getClientRegistration().getClientId(),
                authorizedClient.getClientRegistration().getAuthorizationGrantType().getValue(),
                authorizedClient.getAccessToken().getTokenType().getValue(),
                productEndpoint,
                products.size(),
                products);
    }
}
