package com.example.dispatch.dto;

import java.util.ArrayList;
import java.util.List;

public class DispatchPlanResponse {

    private List<VehicleDispatchDTO> dispatchPlan = new ArrayList<>();

    /**
     * Orders that could not be assigned to any vehicle (e.g. every vehicle
     * lacked sufficient remaining capacity). Included so the caller can see
     * and react to unassignable orders, per the error-handling requirement.
     */
    private List<UnassignedOrderDTO> unassignedOrders = new ArrayList<>();

    public List<VehicleDispatchDTO> getDispatchPlan() {
        return dispatchPlan;
    }

    public void setDispatchPlan(List<VehicleDispatchDTO> dispatchPlan) {
        this.dispatchPlan = dispatchPlan;
    }

    public List<UnassignedOrderDTO> getUnassignedOrders() {
        return unassignedOrders;
    }

    public void setUnassignedOrders(List<UnassignedOrderDTO> unassignedOrders) {
        this.unassignedOrders = unassignedOrders;
    }
}
