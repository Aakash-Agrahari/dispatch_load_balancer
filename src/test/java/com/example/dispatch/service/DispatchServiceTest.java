package com.example.dispatch.service;

import com.example.dispatch.dto.DispatchPlanResponse;
import com.example.dispatch.dto.VehicleDispatchDTO;
import com.example.dispatch.exception.DispatchException;
import com.example.dispatch.model.Order;
import com.example.dispatch.model.Priority;
import com.example.dispatch.model.Vehicle;
import com.example.dispatch.repository.OrderRepository;
import com.example.dispatch.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private DispatchService dispatchService;

    @BeforeEach
    void setUp() {
        dispatchService = new DispatchService(orderRepository, vehicleRepository, new DistanceCalculator());
    }

    @Test
    void throwsWhenNoVehiclesRegistered() {
        when(vehicleRepository.findAll()).thenReturn(Collections.emptyList());

        DispatchException ex = assertThrows(DispatchException.class,
                () -> dispatchService.generateDispatchPlan());
        assertTrue(ex.getMessage().toLowerCase().contains("vehicle"));
    }

    @Test
    void throwsWhenNoOrdersRegistered() {
        when(vehicleRepository.findAll()).thenReturn(List.of(
                new Vehicle("VEH001", 100.0, 28.7041, 77.1025, "Karol Bagh, Delhi, India")));
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        DispatchException ex = assertThrows(DispatchException.class,
                () -> dispatchService.generateDispatchPlan());
        assertTrue(ex.getMessage().toLowerCase().contains("order"));
    }

    @Test
    void highPriorityOrdersAreAssignedBeforeLowerPriorityOnesWhenCapacityIsTight() {
        // A single vehicle with capacity for only one of the two orders.
        Vehicle vehicle = new Vehicle("VEH001", 15.0, 28.6139, 77.2090, "Connaught Place, Delhi, India");

        Order lowPriorityCloser = new Order("ORD_LOW", 28.6140, 77.2091,
                "Connaught Place, Delhi, India", 15.0, Priority.LOW);
        Order highPriorityFarther = new Order("ORD_HIGH", 28.7041, 77.1025,
                "Karol Bagh, Delhi, India", 15.0, Priority.HIGH);

        DispatchPlanResponse response = dispatchService.buildPlan(
                List.of(lowPriorityCloser, highPriorityFarther), List.of(vehicle));

        VehicleDispatchDTO vehiclePlan = response.getDispatchPlan().get(0);
        assertEquals(1, vehiclePlan.getAssignedOrders().size());
        assertEquals("ORD_HIGH", vehiclePlan.getAssignedOrders().get(0).getOrderId());
        assertEquals(1, response.getUnassignedOrders().size());
        assertEquals("ORD_LOW", response.getUnassignedOrders().get(0).getOrderId());
    }

    @Test
    void vehicleCapacityIsNeverExceeded() {
        Vehicle vehicle = new Vehicle("VEH001", 20.0, 28.6139, 77.2090, "Connaught Place, Delhi, India");
        Order order1 = new Order("ORD001", 28.6139, 77.2090, "Connaught Place, Delhi, India", 15.0, Priority.HIGH);
        Order order2 = new Order("ORD002", 28.6139, 77.2090, "Connaught Place, Delhi, India", 10.0, Priority.HIGH);

        DispatchPlanResponse response = dispatchService.buildPlan(List.of(order1, order2), List.of(vehicle));

        VehicleDispatchDTO vehiclePlan = response.getDispatchPlan().get(0);
        assertTrue(vehiclePlan.getTotalLoad() <= vehicle.getCapacity());
        assertEquals(1, response.getUnassignedOrders().size());
    }

    @Test
    void nearestVehicleIsPreferredToMinimizeDistance() {
        Vehicle near = new Vehicle("VEH_NEAR", 100.0, 28.6139, 77.2090, "Connaught Place, Delhi, India");
        Vehicle far = new Vehicle("VEH_FAR", 100.0, 13.0827, 80.2707, "Anna Salai, Chennai, Tamil Nadu, India");

        Order order = new Order("ORD001", 28.6140, 77.2091, "Connaught Place, Delhi, India", 10.0, Priority.HIGH);

        DispatchPlanResponse response = dispatchService.buildPlan(List.of(order), List.of(near, far));

        VehicleDispatchDTO nearPlan = response.getDispatchPlan().stream()
                .filter(v -> v.getVehicleId().equals("VEH_NEAR")).findFirst().orElseThrow();
        VehicleDispatchDTO farPlan = response.getDispatchPlan().stream()
                .filter(v -> v.getVehicleId().equals("VEH_FAR")).findFirst().orElseThrow();

        assertEquals(1, nearPlan.getAssignedOrders().size());
        assertEquals(0, farPlan.getAssignedOrders().size());
    }

    @Test
    void allOrdersUnassignedWhenNoVehicleHasEnoughCapacity() {
        Vehicle vehicle = new Vehicle("VEH001", 5.0, 28.6139, 77.2090, "Connaught Place, Delhi, India");
        Order order = new Order("ORD001", 28.6139, 77.2090, "Connaught Place, Delhi, India", 50.0, Priority.HIGH);

        DispatchPlanResponse response = dispatchService.buildPlan(List.of(order), List.of(vehicle));

        assertEquals(1, response.getUnassignedOrders().size());
        assertEquals("ORD001", response.getUnassignedOrders().get(0).getOrderId());
        assertEquals(0, response.getDispatchPlan().get(0).getAssignedOrders().size());
    }
}
