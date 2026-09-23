package com.cakedelight.notification.event;

public class OrderStatusChangedEvent {

    private Long orderId;
    private String customerName;
    private String oldStatus;
    private String newStatus;

    public OrderStatusChangedEvent() {
    }

    public OrderStatusChangedEvent(
            Long orderId,
            String customerName,
            String oldStatus,
            String newStatus) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
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

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}