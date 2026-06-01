package com.example.orderservice.model;

import java.math.BigDecimal;

public record OrderRecord(
        long id,
        String ownerUsername,
        String productName,
        int quantity,
        BigDecimal totalAmount) {
}
