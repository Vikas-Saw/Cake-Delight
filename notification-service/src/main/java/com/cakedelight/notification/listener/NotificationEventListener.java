package com.cakedelight.notification.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.cakedelight.notification.entity.Notification;
import com.cakedelight.notification.event.OrderCompletedEvent;
import com.cakedelight.notification.event.OrderStatusChangedEvent;
import com.cakedelight.notification.service.NotificationService;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Order completed notification
    @RabbitListener(queues = "order.completed.queue")
    public void handleOrderCompleted(OrderCompletedEvent event) {

        Notification notification = new Notification();

        notification.setOrderId(event.getOrderId());
        notification.setCustomerName(event.getCustomerName());
        notification.setMessage(
                "Order " + event.getOrderId()
                        + " completed successfully."
        );
        notification.setStatus("SENT");
        notification.setAttemptCount(1);

        notificationService.saveNotification(notification);

        System.out.println(
                "Notification created for Order ID: "
                        + event.getOrderId()
        );
    }

    // Order status changed notification
    @RabbitListener(queues = "order.status.changed.queue")
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {

        Notification notification = new Notification();

        notification.setOrderId(event.getOrderId());
        notification.setCustomerName(event.getCustomerName());
        notification.setMessage(
                "Order " + event.getOrderId()
                        + " status changed from "
                        + event.getOldStatus()
                        + " to "
                        + event.getNewStatus()
        );
        notification.setStatus("SENT");
        notification.setAttemptCount(1);

        notificationService.saveNotification(notification);

        System.out.println(
                "Status notification created for Order ID: "
                        + event.getOrderId()
                        + " : "
                        + event.getOldStatus()
                        + " -> "
                        + event.getNewStatus()
        );
    }
}