package com.example.productservice.model;

import java.math.BigDecimal;

public record Product(Long id, String name, String description, BigDecimal price, boolean isPublic) {
}
