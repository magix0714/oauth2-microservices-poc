package com.example.paymentservice.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.paymentservice.model.PaymentRequest;
import com.example.paymentservice.model.PaymentResult;
import com.example.paymentservice.service.OrderOwnershipClient.OrderAccessException;

@Service
public class PaymentService {

    private final Set<Long> paidOrders = ConcurrentHashMap.newKeySet();
    private final OrderOwnershipClient orderOwnershipClient;

    public PaymentService(OrderOwnershipClient orderOwnershipClient) {
        this.orderOwnershipClient = orderOwnershipClient;
    }

    public PaymentResult processPayment(PaymentRequest request, String bearerToken) {
        if (request.amountCents() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount must be positive");
        }

        if (paidOrders.contains(request.orderId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already paid");
        }

        try {
            orderOwnershipClient.checkOrderAccess(request.orderId(), bearerToken);
        } catch (OrderAccessException ex) {
            if (ex.getStatusCode() == 403) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not allowed to pay for this order");
            }
            if (ex.getStatusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Order validation failed");
        }

        paidOrders.add(request.orderId());
        return new PaymentResult(
                request.orderId(),
                request.amountCents(),
                "PAID",
                "Payment accepted");
    }
}
