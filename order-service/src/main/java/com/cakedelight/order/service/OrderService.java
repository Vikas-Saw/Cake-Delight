package com.cakedelight.order.service;

import java.util.List;

import java.util.stream.Collectors;

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
import com.cakedelight.order.event.OrderStatusChangedEvent;

@Service
public class OrderService {

    private final RestClient restClient;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(
            OrderRepository orderRepository,
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

    // =========================================================
    // CREATE ORDER
    // =========================================================

    public Order createOrder(Order order, Long userId) {

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        // Check whether the user already has an active cart
        return orderRepository
                .findFirstByUserIdAndStatusOrderByIdDesc(userId, "PLACED")
                .orElseGet(() -> {

                    // Always take userId from JWT
                    order.setUserId(userId);

                    // Check whether cake exists in Catalog Service
                    try {
                        restClient.get()
                                .uri("/cakes/{id}", order.getCakeId())
                                .retrieve()
                                .toBodilessEntity();

                    } catch (Exception e) {
                        throw new CakeNotFoundException(
                                "Cake not found in Catalog Service"
                        );
                    }

                    order.setStatus("PLACED");

                    return orderRepository.save(order);
                });
    }

    // =========================================================
    // GET ALL ORDERS FOR LOGGED-IN USER
    // =========================================================

    public List<Order> getAllOrders(Long userId) {

        return orderRepository.findAll()
                .stream()
                .filter(order ->
                        userId.equals(order.getUserId()))
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET ORDER BY ID
    // =========================================================

    public Order getOrderById(Long id, Long userId) {

        Order order = orderRepository.findById(id)
                .orElse(null);

        if (order == null) {
            return null;
        }

        // User can only access their own order
        if (!userId.equals(order.getUserId())) {
            return null;
        }

        return order;
    }

    // =========================================================
    // UPDATE ORDER
    // =========================================================

    public Order updateOrder(
            Long id,
            Order order,
            Long userId) {

        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        // Ownership check
        if (!userId.equals(existingOrder.getUserId())) {
            throw new RuntimeException("Unauthorized order access");
        }

        existingOrder.setCustomerName(order.getCustomerName());
        existingOrder.setCakeId(order.getCakeId());
        existingOrder.setQuantity(order.getQuantity());
        existingOrder.setTotalPrice(order.getTotalPrice());

        // Keep userId from JWT
        existingOrder.setUserId(userId);

        return orderRepository.save(existingOrder);
    }
    
    
    
	 // =========================================================
	 // UPDATE ORDER STATUS
	 // =========================================================
	
	 
    
    public Order updateOrderStatus(Long id, String newStatus, Long userId) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!userId.equals(order.getUserId())) {
            throw new RuntimeException("Unauthorized order access");
        }

        String currentStatus = order.getStatus();

        boolean validTransition =
                (currentStatus.equals("PLACED")
                        && (newStatus.equals("CONFIRMED")
                        || newStatus.equals("CANCELLED")))

                || (currentStatus.equals("CONFIRMED")
                        && (newStatus.equals("PREPARING")
                        || newStatus.equals("CANCELLED")))

                || (currentStatus.equals("PREPARING")
                        && newStatus.equals("OUT_FOR_DELIVERY"))

                || (currentStatus.equals("OUT_FOR_DELIVERY")
                        && newStatus.equals("DELIVERED"));

        if (!validTransition) {
            throw new RuntimeException(
                    "Invalid order status transition: "
                            + currentStatus + " -> " + newStatus);
        }

        // Save the new status
        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        // Publish status-change event to RabbitMQ
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                savedOrder.getId(),
                savedOrder.getCustomerName(),
                currentStatus,
                newStatus
        );

        orderEventPublisher.publishOrderStatusChanged(event);

        return savedOrder;
    }
    
    
    
    // =========================================================
    // DELETE ORDER
    // =========================================================

    public void deleteOrder(Long id, Long userId) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (!userId.equals(order.getUserId())) {
            throw new RuntimeException("Unauthorized order access");
        }

        orderRepository.deleteById(id);
    }

    // =========================================================
    // BASKET - ADD ITEM
    // =========================================================

    public OrderItem addToBasket(
            OrderItem item,
            Long userId) {

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        // Verify order belongs to logged-in user
        Order order = orderRepository.findById(item.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (!userId.equals(order.getUserId())) {
            throw new RuntimeException(
                    "Unauthorized basket access");
        }

        // Always take userId from JWT
        item.setUserId(userId);

        // Check whether cake exists
        try {

            restClient.get()
                    .uri("/cakes/{id}", item.getCakeId())
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {

            throw new CakeNotFoundException(
                    "Cake not found in Catalog Service"
            );
        }

        return orderItemRepository.save(item);
    }

    // =========================================================
    // VIEW BASKET
    // =========================================================

    public List<OrderItem> getBasket(
            Long orderId,
            Long userId) {

        // Verify order ownership
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        if (!userId.equals(order.getUserId())) {
            throw new RuntimeException(
                    "Unauthorized basket access");
        }

        return orderItemRepository.findByOrderId(orderId)
                .stream()
                .filter(item ->
                        userId.equals(item.getUserId()))
                .collect(Collectors.toList());
    }

    // =========================================================
    // UPDATE BASKET ITEM
    // =========================================================

    public OrderItem updateBasketItem(
            Long itemId,
            OrderItem item,
            Long userId) {

        OrderItem existingItem =
                orderItemRepository.findById(itemId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Basket item not found"));

        // Ownership check
        if (!userId.equals(existingItem.getUserId())) {
            throw new RuntimeException(
                    "Unauthorized basket access");
        }

        existingItem.setCakeId(item.getCakeId());
        existingItem.setQuantity(item.getQuantity());

        // Keep original userId
        existingItem.setUserId(userId);

        return orderItemRepository.save(existingItem);
    }

    // =========================================================
    // REMOVE BASKET ITEM
    // =========================================================

    public void removeFromBasket(
            Long itemId,
            Long userId) {

        OrderItem item =
                orderItemRepository.findById(itemId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Basket item not found"));

        // Ownership check
        if (!userId.equals(item.getUserId())) {
            throw new RuntimeException(
                    "Unauthorized basket access");
        }

        orderItemRepository.deleteById(itemId);
    }

    // =========================================================
    // CHECKOUT
    // =========================================================

    public Order checkout(
            Long orderId,
            Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        // Ownership check
        if (!userId.equals(order.getUserId())) {
            throw new RuntimeException(
                    "Unauthorized order access");
        }

        List<OrderItem> items =
                orderItemRepository.findByOrderId(orderId)
                        .stream()
                        .filter(item ->
                                userId.equals(item.getUserId()))
                        .collect(Collectors.toList());

        if (items.isEmpty()) {
            throw new RuntimeException("Basket is empty");
        }

        double total = 0.0;

        // Calculate total from Catalog Service
        for (OrderItem item : items) {

            try {

                CakeResponse cake = restClient.get()
                        .uri("/cakes/{id}", item.getCakeId())
                        .retrieve()
                        .body(CakeResponse.class);

                if (cake == null ||
                        cake.getPrice() == null) {

                    throw new RuntimeException(
                            "Cake price not found");
                }

                total += cake.getPrice()
                        * item.getQuantity();

            } catch (Exception e) {

                throw new CakeNotFoundException(
                        "Cake not found in Catalog Service");
            }
        }

        order.setTotalPrice(total);
        order.setStatus("CONFIRMED");

        Order savedOrder =
                orderRepository.save(order);

        // Publish Order Completed Event
        OrderCompletedEvent event =
                new OrderCompletedEvent(
                        savedOrder.getId(),
                        savedOrder.getCustomerName(),
                        savedOrder.getTotalPrice(),
                        savedOrder.getStatus()
                );

        orderEventPublisher.publishOrderCompleted(event);

        return savedOrder;
    }
}