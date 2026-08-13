package com.cakedelight.order.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cakedelight.order.entity.Order;
import com.cakedelight.order.entity.OrderItem;
import com.cakedelight.order.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Create Order
    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    // Get All Orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // Get Order By ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {

        Order order = orderService.getOrderById(id);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(order);
    }

    // Update Order
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long id,
            @RequestBody Order order) {

        try {
            return ResponseEntity.ok(
                    orderService.updateOrder(id, order)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete Order
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {

        orderService.deleteOrder(id);

        return ResponseEntity.ok("Order deleted successfully");
    }

    // Add Item to Basket
    @PostMapping("/basket")
    public OrderItem addToBasket(@RequestBody OrderItem item) {
        return orderService.addToBasket(item);
    }

    // View Basket
    @GetMapping("/basket/{orderId}")
    public List<OrderItem> getBasket(@PathVariable Long orderId) {
        return orderService.getBasket(orderId);
    }

    // Update Basket Item
    @PutMapping("/basket/item/{itemId}")
    public ResponseEntity<OrderItem> updateBasketItem(
            @PathVariable Long itemId,
            @RequestBody OrderItem item) {

        try {
            return ResponseEntity.ok(
                    orderService.updateBasketItem(itemId, item)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Remove Item from Basket
    @DeleteMapping("/basket/item/{itemId}")
    public ResponseEntity<String> removeFromBasket(
            @PathVariable Long itemId) {

        try {
            orderService.removeFromBasket(itemId);

            return ResponseEntity.ok(
                    "Basket item removed successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/checkout/{orderId}")
    public ResponseEntity<Order> checkout(
            @PathVariable Long orderId) {

        try {
            return ResponseEntity.ok(
                    orderService.checkout(orderId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}