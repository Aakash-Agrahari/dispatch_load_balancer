package com.example.dispatch.controller;

import com.example.dispatch.dto.ApiResponse;
import com.example.dispatch.dto.VehiclesRequest;
import com.example.dispatch.model.Vehicle;
import com.example.dispatch.repository.VehicleRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispatch/vehicles")
public class VehicleController {

    private final VehicleRepository vehicleRepository;

    public VehicleController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Accepts a batch of fleet vehicles. Existing vehicles with the same
     * vehicleId are updated (upsert) rather than duplicated.
     */
    @PostMapping
    public ResponseEntity<ApiResponse> submitVehicles(@Valid @RequestBody VehiclesRequest request) {
        vehicleRepository.saveAll(request.getVehicles());
        return ResponseEntity.ok(ApiResponse.success("Vehicle details accepted."));
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> listVehicles() {
        return ResponseEntity.ok(vehicleRepository.findAll());
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> clearVehicles() {
        vehicleRepository.deleteAll();
        return ResponseEntity.ok(ApiResponse.success("All vehicles cleared."));
    }
}
