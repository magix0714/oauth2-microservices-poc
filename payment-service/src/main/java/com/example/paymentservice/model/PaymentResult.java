package com.example.paymentservice.model;

public record PaymentResult(
        long orderId,
        int amountCents,
        String status,
        String message) {
}
