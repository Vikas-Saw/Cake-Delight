package com.cakedelight.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cakedelight.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}