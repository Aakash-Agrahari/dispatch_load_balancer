package com.example.dispatch.dto;

import com.example.dispatch.model.Vehicle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class VehiclesRequest {

    @NotEmpty(message = "vehicles list must not be empty")
    @Valid
    private List<Vehicle> vehicles;

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }
}
