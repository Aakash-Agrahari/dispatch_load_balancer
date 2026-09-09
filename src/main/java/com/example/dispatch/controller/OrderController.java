package com.example.dispatch.controller;

import com.example.dispatch.dto.ApiResponse;
import com.example.dispatch.dto.OrdersRequest;
import com.example.dispatch.model.Order;
import com.example.dispatch.repository.OrderRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispatch/orders")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Accepts a batch of delivery orders. Existing orders with the same
     * orderId are updated (upsert) rather than duplicated.
     */
    @PostMapping
    public ResponseEntity<ApiResponse> submitOrders(@Valid @RequestBody OrdersRequest request) {
        orderRepository.saveAll(request.getOrders());
        return ResponseEntity.ok(ApiResponse.success("Delivery orders accepted."));
    }

    @GetMapping
    public ResponseEntity<List<Order>> listOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> clearOrders() {
        orderRepository.deleteAll();
        return ResponseEntity.ok(ApiResponse.success("All orders cleared."));
    }
}
