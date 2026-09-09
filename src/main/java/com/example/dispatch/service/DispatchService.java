package com.example.dispatch.service;

import com.example.dispatch.dto.AssignedOrderDTO;
import com.example.dispatch.dto.DispatchPlanResponse;
import com.example.dispatch.dto.UnassignedOrderDTO;
import com.example.dispatch.dto.VehicleDispatchDTO;
import com.example.dispatch.exception.DispatchException;
import com.example.dispatch.model.Order;
import com.example.dispatch.model.Priority;
import com.example.dispatch.model.Vehicle;
import com.example.dispatch.repository.OrderRepository;
import com.example.dispatch.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Builds an optimized dispatch plan assigning delivery orders to vehicles.
 *
 * Strategy (a greedy nearest-neighbour heuristic, which is the standard
 * practical approach to this NP-hard capacitated-VRP-style problem):
 *
 * 1. Orders are processed in priority order: HIGH, then MEDIUM, then LOW.
 *    Within a priority tier, heavier orders are considered first so that
 *    large orders are not left stranded once vehicles fill up.
 * 2. For each order, every vehicle that still has enough remaining capacity
 *    is considered. The vehicle whose *current position* (its starting
 *    location, or the location of the last order assigned to it) is
 *    nearest to the order - by Haversine distance - is chosen. This keeps
 *    each vehicle's route local and minimizes total travel distance.
 * 3. The chosen vehicle's remaining capacity is reduced, its position is
 *    advanced to the order's location, and the leg distance is added to
 *    its running total.
 * 4. Orders that cannot be placed on any vehicle (no vehicle has enough
 *    remaining capacity) are reported back as unassigned rather than
 *    silently dropped.
 */
@Service
public class DispatchService {

    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final DistanceCalculator distanceCalculator;

    public DispatchService(OrderRepository orderRepository,
                            VehicleRepository vehicleRepository,
                            DistanceCalculator distanceCalculator) {
        this.orderRepository = orderRepository;
        this.vehicleRepository = vehicleRepository;
        this.distanceCalculator = distanceCalculator;
    }

    public DispatchPlanResponse generateDispatchPlan() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<Order> orders = orderRepository.findAll();

        if (vehicles.isEmpty()) {
            throw new DispatchException("No vehicles registered. POST /api/dispatch/vehicles first.");
        }
        if (orders.isEmpty()) {
            throw new DispatchException("No delivery orders registered. POST /api/dispatch/orders first.");
        }

        return buildPlan(orders, vehicles);
    }

    /**
     * Pure function (no repository access) so it is easy to unit test with
     * hand-built in-memory lists.
     */
    public DispatchPlanResponse buildPlan(List<Order> orders, List<Vehicle> vehicles) {
        List<VehicleState> states = new ArrayList<>();
        for (Vehicle v : vehicles) {
            states.add(new VehicleState(v));
        }

        List<Order> sortedOrders = new ArrayList<>(orders);
        sortedOrders.sort(
                Comparator.comparing((Order o) -> o.getPriority(), Comparator.comparingInt(this::priorityRank))
                        .thenComparing(Order::getPackageWeight, Comparator.reverseOrder())
        );

        List<UnassignedOrderDTO> unassigned = new ArrayList<>();

        for (Order order : sortedOrders) {
            VehicleState best = null;
            double bestDistance = Double.MAX_VALUE;

            for (VehicleState state : states) {
                if (state.remainingCapacity < order.getPackageWeight()) {
                    continue;
                }
                double distance = distanceCalculator.haversineDistanceKm(
                        state.currentLatitude, state.currentLongitude,
                        order.getLatitude(), order.getLongitude());

                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = state;
                }
            }

            if (best == null) {
                unassigned.add(new UnassignedOrderDTO(order.getOrderId(),
                        "No vehicle with sufficient remaining capacity (" + order.getPackageWeight() + ") available"));
                continue;
            }

            best.assign(order, bestDistance);
        }

        DispatchPlanResponse response = new DispatchPlanResponse();
        List<VehicleDispatchDTO> plan = new ArrayList<>();
        for (VehicleState state : states) {
            plan.add(state.toDTO());
        }
        response.setDispatchPlan(plan);
        response.setUnassignedOrders(unassigned);
        return response;
    }

    private int priorityRank(Priority priority) {
        switch (priority) {
            case HIGH:
                return 0;
            case MEDIUM:
                return 1;
            case LOW:
            default:
                return 2;
        }
    }

    /**
     * Mutable per-vehicle working state used while greedily building the plan.
     */
    private static class VehicleState {
        private final Vehicle vehicle;
        private double remainingCapacity;
        private double currentLatitude;
        private double currentLongitude;
        private double totalLoad = 0.0;
        private double totalDistanceKm = 0.0;
        private final List<Order> assignedOrders = new ArrayList<>();

        VehicleState(Vehicle vehicle) {
            this.vehicle = vehicle;
            this.remainingCapacity = vehicle.getCapacity();
            this.currentLatitude = vehicle.getCurrentLatitude();
            this.currentLongitude = vehicle.getCurrentLongitude();
        }

        void assign(Order order, double legDistanceKm) {
            remainingCapacity -= order.getPackageWeight();
            totalLoad += order.getPackageWeight();
            totalDistanceKm += legDistanceKm;
            currentLatitude = order.getLatitude();
            currentLongitude = order.getLongitude();
            assignedOrders.add(order);
        }

        VehicleDispatchDTO toDTO() {
            VehicleDispatchDTO dto = new VehicleDispatchDTO(vehicle.getVehicleId());
            dto.setTotalLoad(totalLoad);
            dto.setTotalDistance(String.format(Locale.US, "%.2f km", totalDistanceKm));
            List<AssignedOrderDTO> orderDTOs = new ArrayList<>();
            for (Order o : assignedOrders) {
                orderDTOs.add(AssignedOrderDTO.fromOrder(o));
            }
            dto.setAssignedOrders(orderDTOs);
            return dto;
        }
    }
}
