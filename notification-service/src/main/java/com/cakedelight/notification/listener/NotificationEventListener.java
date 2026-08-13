package com.cakedelight.notification.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.cakedelight.notification.entity.Notification;
import com.cakedelight.notification.event.OrderCompletedEvent;
import com.cakedelight.notification.service.NotificationService;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "order.completed.queue")
    public void handleOrderCompleted(OrderCompletedEvent event) {

        Notification notification = new Notification();

        notification.setOrderId(event.getOrderId());
        notification.setCustomerName(event.getCustomerName());
        notification.setMessage(
                "Order " + event.getOrderId() + " completed successfully."
        );
        notification.setStatus("SENT");

        notificationService.saveNotification(notification);

        System.out.println(
                "Notification created for Order ID: " + event.getOrderId()
        );
    }
}