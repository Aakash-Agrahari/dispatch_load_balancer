package com.example.dispatch.dto;

import java.util.ArrayList;
import java.util.List;

public class VehicleDispatchDTO {

    private String vehicleId;
    private Double totalLoad = 0.0;
    private String totalDistance = "0 km";
    private List<AssignedOrderDTO> assignedOrders = new ArrayList<>();

    public VehicleDispatchDTO() {
    }

    public VehicleDispatchDTO(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Double getTotalLoad() {
        return totalLoad;
    }

    public void setTotalLoad(Double totalLoad) {
        this.totalLoad = totalLoad;
    }

    public String getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(String totalDistance) {
        this.totalDistance = totalDistance;
    }

    public List<AssignedOrderDTO> getAssignedOrders() {
        return assignedOrders;
    }

    public void setAssignedOrders(List<AssignedOrderDTO> assignedOrders) {
        this.assignedOrders = assignedOrders;
    }
}
