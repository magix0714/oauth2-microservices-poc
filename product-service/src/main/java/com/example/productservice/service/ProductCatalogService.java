package com.example.productservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.productservice.model.CreateProductRequest;
import com.example.productservice.model.Product;

@Service
public class ProductCatalogService {

    private final AtomicLong idSequence = new AtomicLong(1000);
    private final CopyOnWriteArrayList<Product> products = new CopyOnWriteArrayList<>();

    public ProductCatalogService() {
        products.add(new Product(idSequence.getAndIncrement(), "Coffee Beans", "Public teaser item", new java.math.BigDecimal("9.90"), true));
        products.add(new Product(idSequence.getAndIncrement(), "Premium Grinder", "Authenticated product", new java.math.BigDecimal("149.00"), false));
    }

    public List<Product> publicProducts() {
        return products.stream().filter(Product::isPublic).toList();
    }

    public List<Product> allProducts() {
        return new ArrayList<>(products);
    }

    public Product addProduct(CreateProductRequest request) {
        Product product = new Product(
                idSequence.getAndIncrement(),
                request.name(),
                request.description(),
                request.price(),
                request.isPublic());
        products.add(product);
        return product;
    }
}
