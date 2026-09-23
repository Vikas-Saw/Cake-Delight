package com.cakedelight.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cakedelight.notification.entity.Notification;
import com.cakedelight.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Save notification
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    // Get all notifications
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    // Get notifications for a particular order
    public List<Notification> getNotificationsByOrderId(Long orderId) {
        return notificationRepository.findByOrderId(orderId);
    }

    // Get notification by ID
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }
}