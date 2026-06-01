package com.example.paymentservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.paymentservice.model.PaymentRequest;
import com.example.paymentservice.model.PaymentResult;
import com.example.paymentservice.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResult pay(@Valid @RequestBody PaymentRequest request, JwtAuthenticationToken authentication) {
        String token = authentication.getToken().getTokenValue();
        return paymentService.processPayment(request, token);
    }
}
