package com.cakedelight.notification.event;

public class OrderCompletedEvent {

    private Long orderId;
    private String customerName;
    private Double totalPrice;
    private String status;

    public OrderCompletedEvent() {
    }

    public OrderCompletedEvent(
            Long orderId,
            String customerName,
            Double totalPrice,
            String status) {

        this.orderId = orderId;
        this.customerName = customerName;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}