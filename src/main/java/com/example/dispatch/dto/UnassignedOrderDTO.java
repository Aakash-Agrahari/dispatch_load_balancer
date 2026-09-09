package com.example.dispatch.dto;

public class UnassignedOrderDTO {

    private String orderId;
    private String reason;

    public UnassignedOrderDTO() {
    }

    public UnassignedOrderDTO(String orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
