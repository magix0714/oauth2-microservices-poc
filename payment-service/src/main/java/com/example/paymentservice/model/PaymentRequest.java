package com.example.paymentservice.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull Long orderId,
        @NotNull @Min(1) Integer amountCents) {
}
