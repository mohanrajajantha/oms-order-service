package com.oms.order_service.controller;

import com.oms.order_service.dto.OrderRequestDto;
import com.oms.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders") // We can also use Spring Boot 4's native API versioning here!
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> placeOrder(@Valid @RequestBody OrderRequestDto orderRequest) {
        log.info("Received order request from Salesforce for order: {}", orderRequest.getOrderNo());
        String response = orderService.placeOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}