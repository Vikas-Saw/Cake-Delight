package com.cakedelight.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cakedelight.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    Optional<Order> findFirstByUserIdAndStatusOrderByIdDesc(
            Long userId,
            String status
    );
}