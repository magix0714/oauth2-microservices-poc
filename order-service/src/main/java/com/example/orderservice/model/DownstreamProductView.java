package com.example.orderservice.model;

import java.math.BigDecimal;

public record DownstreamProductView(
        long id,
        String name,
        String description,
        BigDecimal price,
        boolean isPublic) {
}
