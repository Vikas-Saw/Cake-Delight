package com.cakedelight.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cakedelight.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByOrderId(Long orderId);
}