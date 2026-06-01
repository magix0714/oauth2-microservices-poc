package com.example.paymentservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrderOwnershipClient {

    private final RestClient restClient;

    public OrderOwnershipClient(@Value("${services.order-service-base-url:http://localhost:8084}") String orderServiceBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(orderServiceBaseUrl).build();
    }

    public int checkOrderAccess(long orderId, String bearerToken) {
        return restClient.get()
                .uri("/orders/{id}", orderId)
                .headers(headers -> headers.setBearerAuth(bearerToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new OrderAccessException(response.getStatusCode().value());
                })
                .toBodilessEntity()
                .getStatusCode()
                .value();
    }

    public static class OrderAccessException extends RuntimeException {
        private final int statusCode;

        public OrderAccessException(int statusCode) {
            super("Order service returned status " + statusCode);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
