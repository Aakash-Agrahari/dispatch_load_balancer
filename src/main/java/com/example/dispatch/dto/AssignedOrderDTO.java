package com.example.dispatch.dto;

import com.example.dispatch.model.Order;
import com.example.dispatch.model.Priority;

public class AssignedOrderDTO {

    private String orderId;
    private Double latitude;
    private Double longitude;
    private String address;
    private Double packageWeight;
    private Priority priority;

    public AssignedOrderDTO() {
    }

    public static AssignedOrderDTO fromOrder(Order order) {
        AssignedOrderDTO dto = new AssignedOrderDTO();
        dto.orderId = order.getOrderId();
        dto.latitude = order.getLatitude();
        dto.longitude = order.getLongitude();
        dto.address = order.getAddress();
        dto.packageWeight = order.getPackageWeight();
        dto.priority = order.getPriority();
        return dto;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(Double packageWeight) {
        this.packageWeight = packageWeight;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
