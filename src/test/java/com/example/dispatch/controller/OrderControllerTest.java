package com.example.dispatch.controller;

import com.example.dispatch.repository.OrderRepository;
import com.example.dispatch.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @BeforeEach
    void cleanUp() {
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    @Test
    void acceptsValidOrders() throws Exception {
        String body = """
                {
                  "orders": [
                    {
                      "orderId": "ORD001",
                      "latitude": 12.9716,
                      "longitude": 77.5946,
                      "address": "MG Road, Bangalore, Karnataka, India",
                      "packageWeight": 10,
                      "priority": "HIGH"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        mockMvc.perform(get("/api/dispatch/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value("ORD001"));
    }

    @Test
    void rejectsOrderWithMissingRequiredFields() throws Exception {
        String body = """
                {
                  "orders": [
                    { "orderId": "", "packageWeight": -5 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void rejectsEmptyOrdersList() throws Exception {
        String body = "{ \"orders\": [] }";

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upsertsOrderWithSameId() throws Exception {
        String body1 = """
                { "orders": [ { "orderId": "ORD001", "latitude": 12.9, "longitude": 77.5,
                  "address": "A", "packageWeight": 10, "priority": "HIGH" } ] }
                """;
        String body2 = """
                { "orders": [ { "orderId": "ORD001", "latitude": 12.9, "longitude": 77.5,
                  "address": "A", "packageWeight": 25, "priority": "LOW" } ] }
                """;

        mockMvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON).content(body1))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON).content(body2))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/dispatch/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].packageWeight").value(25));
    }
}
