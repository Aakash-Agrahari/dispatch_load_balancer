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
class DispatchControllerTest {

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
    void returnsBadRequestWhenNothingRegistered() throws Exception {
        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void endToEndDispatchPlanIsGenerated() throws Exception {
        String vehiclesBody = """
                {
                  "vehicles": [
                    { "vehicleId": "VEH001", "capacity": 100, "currentLatitude": 12.9716,
                      "currentLongitude": 77.6413, "currentAddress": "Indiranagar, Bangalore, Karnataka, India" }
                  ]
                }
                """;
        String ordersBody = """
                {
                  "orders": [
                    { "orderId": "ORD001", "latitude": 12.9716, "longitude": 77.5946,
                      "address": "MG Road, Bangalore, Karnataka, India", "packageWeight": 10, "priority": "HIGH" }
                  ]
                }
                """;

        mockMvc.perform(post("/api/dispatch/vehicles").contentType(MediaType.APPLICATION_JSON).content(vehiclesBody))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON).content(ordersBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dispatchPlan[0].vehicleId").value("VEH001"))
                .andExpect(jsonPath("$.dispatchPlan[0].assignedOrders[0].orderId").value("ORD001"))
                .andExpect(jsonPath("$.dispatchPlan[0].totalLoad").value(10.0))
                .andExpect(jsonPath("$.unassignedOrders").isEmpty());
    }

    @Test
    void unassignableOrderIsReportedNotSilentlyDropped() throws Exception {
        String vehiclesBody = """
                { "vehicles": [ { "vehicleId": "VEH001", "capacity": 5, "currentLatitude": 12.9716,
                  "currentLongitude": 77.6413, "currentAddress": "Indiranagar, Bangalore, Karnataka, India" } ] }
                """;
        String ordersBody = """
                { "orders": [ { "orderId": "ORD001", "latitude": 12.9716, "longitude": 77.5946,
                  "address": "MG Road, Bangalore, Karnataka, India", "packageWeight": 50, "priority": "HIGH" } ] }
                """;

        mockMvc.perform(post("/api/dispatch/vehicles").contentType(MediaType.APPLICATION_JSON).content(vehiclesBody))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/dispatch/orders").contentType(MediaType.APPLICATION_JSON).content(ordersBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unassignedOrders[0].orderId").value("ORD001"))
                .andExpect(jsonPath("$.dispatchPlan[0].assignedOrders").isEmpty());
    }
}
