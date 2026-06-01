package com.example.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    RouteLocator gatewayRoutes(
            RouteLocatorBuilder builder,
            @Value("${PRODUCT_SERVICE_URI:http://localhost:8082}") String productServiceUri,
            @Value("${ORDER_SERVICE_URI:http://localhost:8084}") String orderServiceUri,
            @Value("${PAYMENT_SERVICE_URI:http://localhost:8085}") String paymentServiceUri) {

        return builder.routes()
                .route("product-service", r -> r
                        .path("/api/products", "/api/products/**")
                        .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
                        .uri(productServiceUri))
                .route("order-service", r -> r
                        .path("/api/orders", "/api/orders/**")
                        .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
                        .uri(orderServiceUri))
                .route("payment-service", r -> r
                        .path("/api/payments", "/api/payments/**")
                        .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
                        .uri(paymentServiceUri))
                .route("admin-product-write", r -> r
                        .path("/api/admin/products", "/api/admin/products/**")
                        .filters(f -> f.rewritePath("/api/admin/(?<segment>.*)", "/${segment}"))
                        .uri(productServiceUri))
                .build();
    }
}
