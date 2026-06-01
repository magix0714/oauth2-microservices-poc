package com.example.orderservice.model;

import java.util.List;

public record ServiceToServiceCallResult(
        String clientId,
        String grantType,
        String tokenType,
        String productServiceEndpoint,
        int productCount,
        List<DownstreamProductView> products) {
}
