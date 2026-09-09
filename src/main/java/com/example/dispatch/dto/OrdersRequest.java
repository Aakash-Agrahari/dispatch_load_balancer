package com.example.dispatch.dto;

import com.example.dispatch.model.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class OrdersRequest {

    @NotEmpty(message = "orders list must not be empty")
    @Valid
    private List<Order> orders;

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
