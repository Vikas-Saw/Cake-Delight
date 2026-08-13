package com.cakedelight.order.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.cakedelight.order.entity.Order;
import com.cakedelight.order.entity.OrderItem;
import com.cakedelight.order.entity.CakeResponse;

import com.cakedelight.order.exception.CakeNotFoundException;
import com.cakedelight.order.repository.OrderItemRepository;
import com.cakedelight.order.repository.OrderRepository;

import com.cakedelight.order.event.OrderCompletedEvent;
import com.cakedelight.order.event.OrderEventPublisher;

@Service
public class OrderService {

    private final RestClient restClient;
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderEventPublisher orderEventPublisher) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderEventPublisher = orderEventPublisher;

        this.restClient = RestClient.builder()
                .baseUrl(System.getenv().getOrDefault(
                        "CATALOG_SERVICE_URL",
                        "http://localhost:8080"
                ))
                .build();
    }

    // Create Order
    public Order createOrder(Order order) {

        // Check whether cake exists in Catalog Service
        try {
            restClient.get()
                    .uri("/cakes/{id}", order.getCakeId())
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            throw new CakeNotFoundException(
                    "Cake not found in Catalog Service");
        }

        order.setStatus("PLACED");

        return orderRepository.save(order);
    }

    // Get All Orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get Order By ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    // Update Order
    public Order updateOrder(Long id, Order order) {

        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        existingOrder.setCustomerName(order.getCustomerName());
        existingOrder.setCakeId(order.getCakeId());
        existingOrder.setQuantity(order.getQuantity());
        existingOrder.setTotalPrice(order.getTotalPrice());
        existingOrder.setStatus(order.getStatus());

        return orderRepository.save(existingOrder);
    }

    // Delete Order
    public void deleteOrder(Long id) {

        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found");
        }

        orderRepository.deleteById(id);
    }

    // ==============================
    // BASKET OPERATIONS
    // ==============================

    // Add Item to Basket
    public OrderItem addToBasket(OrderItem item) {

        // Check whether cake exists in Catalog Service
        try {
            restClient.get()
                    .uri("/cakes/{id}", item.getCakeId())
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            throw new CakeNotFoundException(
                    "Cake not found in Catalog Service");
        }

        return orderItemRepository.save(item);
    }

    // View Basket
    public List<OrderItem> getBasket(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    // Update Basket Item
    public OrderItem updateBasketItem(Long itemId, OrderItem item) {

        OrderItem existingItem = orderItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new RuntimeException("Basket item not found"));

        existingItem.setCakeId(item.getCakeId());
        existingItem.setQuantity(item.getQuantity());

        return orderItemRepository.save(existingItem);
    }

    // Remove Item from Basket
    public void removeFromBasket(Long itemId) {

        if (!orderItemRepository.existsById(itemId)) {
            throw new RuntimeException("Basket item not found");
        }

        orderItemRepository.deleteById(itemId);
    }
    
    // Checkout Order
    public Order checkout(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        if (items.isEmpty()) {
            throw new RuntimeException("Basket is empty");
        }

        double total = 0.0;

        for (OrderItem item : items) {

            try {
                CakeResponse cake = restClient.get()
                        .uri("/cakes/{id}", item.getCakeId())
                        .retrieve()
                        .body(CakeResponse.class);

                if (cake == null || cake.getPrice() == null) {
                    throw new RuntimeException("Cake price not found");
                }

                total += cake.getPrice() * item.getQuantity();

            } catch (Exception e) {
                throw new CakeNotFoundException(
                        "Cake not found in Catalog Service");
            }
        }

        order.setTotalPrice(total);
        order.setStatus("CONFIRMED");

        Order savedOrder = orderRepository.save(order);

        OrderCompletedEvent event = new OrderCompletedEvent(
                savedOrder.getId(),
                savedOrder.getCustomerName(),
                savedOrder.getTotalPrice(),
                savedOrder.getStatus()
        );

        orderEventPublisher.publishOrderCompleted(event);

        return savedOrder;
    }

}