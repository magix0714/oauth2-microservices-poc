package com.example.orderservice.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.example.orderservice.model.CreateOrderRequest;
import com.example.orderservice.model.OrderRecord;

@Service
public class InMemoryOrderService {

    private final AtomicLong sequence = new AtomicLong(2000);
    private final CopyOnWriteArrayList<OrderRecord> orders = new CopyOnWriteArrayList<>();

    public InMemoryOrderService() {
        // Starts empty so ownership demos can be based on orders created in-session.
    }

    public List<OrderRecord> listOwnedOrders(String username) {
        return orders.stream()
                .filter(order -> Objects.equals(order.ownerUsername(), username))
                .sorted(Comparator.comparingLong(OrderRecord::id))
                .toList();
    }

    public Optional<OrderRecord> getOwnedOrder(long id, String username) {
        OrderRecord order = orders.stream()
                .filter(item -> item.id() == id)
                .findFirst()
                .orElse(null);
        if (order == null) {
            return Optional.empty();
        }
        if (!Objects.equals(order.ownerUsername(), username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to authenticated user");
        }
        return Optional.of(order);
    }

    public OrderRecord createOrder(CreateOrderRequest request, String username) {
        long id = sequence.incrementAndGet();
        BigDecimal total = new BigDecimal("9.90").multiply(new BigDecimal(request.quantity()));
        OrderRecord order = new OrderRecord(id, username, request.productName(), request.quantity(), total);
        orders.add(order);
        return order;
    }

    public List<OrderRecord> allOrdersForDebug() {
        return new ArrayList<>(orders);
    }
}
