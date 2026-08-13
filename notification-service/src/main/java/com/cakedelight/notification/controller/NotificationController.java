package com.cakedelight.notification.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.cakedelight.notification.entity.Notification;
import com.cakedelight.notification.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Get all notifications
    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    // Get notification by ID
    @GetMapping("/{id}")
    public Notification getNotificationById(@PathVariable Long id) {
        return notificationService.getNotificationById(id);
    }

    // Get notifications for a particular order
    @GetMapping("/order/{orderId}")
    public List<Notification> getNotificationsByOrderId(
            @PathVariable Long orderId) {

        return notificationService.getNotificationsByOrderId(orderId);
    }
}