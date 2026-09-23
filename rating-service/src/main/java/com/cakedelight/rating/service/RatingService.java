package com.cakedelight.rating.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.cakedelight.rating.entity.Rating;
import com.cakedelight.rating.repository.RatingRepository;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RestClient orderServiceClient;

    public RatingService(
            RatingRepository ratingRepository,
            RestClient orderServiceClient) {

        this.ratingRepository = ratingRepository;
        this.orderServiceClient = orderServiceClient;
    }

    // Submit a rating only after successful delivery
    public Rating addRating(
            Rating rating,
            String authorization) {

        if (rating.getCakeId() == null) {
            throw new RuntimeException("Cake ID is required");
        }

        if (rating.getCustomerName() == null
                || rating.getCustomerName().isBlank()) {
            throw new RuntimeException("Customer name is required");
        }

        if (rating.getOrderId() == null) {
            throw new RuntimeException("Order ID is required");
        }

        if (rating.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }

        if (rating.getRating() == null
                || rating.getRating() < 1
                || rating.getRating() > 5) {
            throw new RuntimeException(
                    "Rating must be between 1 and 5");
        }

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Missing JWT token");
        }

        // Get order from Order Service
        OrderResponse order = orderServiceClient
                .get()
                .uri("/orders/{id}", rating.getOrderId())
                .header("Authorization", authorization)
                .retrieve()
                .body(OrderResponse.class);

        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        // Verify user ownership
        if (order.getUserId() == null
                || !rating.getUserId().equals(order.getUserId())) {
            throw new RuntimeException(
                    "You cannot rate another user's order");
        }

        // Only delivered orders can be rated
        if (!"DELIVERED".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException(
                    "Cake can be rated only after order is delivered");
        }

        // Verify cake belongs to the order
        OrderItemResponse[] items = orderServiceClient
                .get()
                .uri("/orders/basket/{orderId}", rating.getOrderId())
                .header("Authorization", authorization)
                .retrieve()
                .body(OrderItemResponse[].class);

        boolean cakeExistsInOrder = false;

        if (items != null) {
            for (OrderItemResponse item : items) {

                if (item.getCakeId() != null
                        && item.getCakeId().equals(rating.getCakeId())) {

                    cakeExistsInOrder = true;
                    break;
                }
            }
        }

        if (!cakeExistsInOrder) {
            throw new RuntimeException(
                    "This cake was not purchased in the selected order");
        }
        
        if (ratingRepository.existsByUserIdAndOrderIdAndCakeId(
                rating.getUserId(),
                rating.getOrderId(),
                rating.getCakeId())) {

            throw new RuntimeException(
                    "You have already rated this cake for this order");
        }

        return ratingRepository.save(rating);
    }

    // Get all ratings
    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }
    
    public List<Rating> getRatingsByUserId(Long userId) {
        return ratingRepository.findByUserId(userId);
    }

    // Get ratings for a particular cake
    public List<Rating> getRatingsByCakeId(Long cakeId) {
        return ratingRepository.findByCakeId(cakeId);
    }

    // Calculate average rating
    public double getAverageRating(Long cakeId) {

        List<Rating> ratings =
                ratingRepository.findByCakeId(cakeId);

        if (ratings.isEmpty()) {
            return 0.0;
        }

        return ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
    }

    // Response class for Order Service
    public static class OrderResponse {

        private Long id;
        private Long userId;
        private String status;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    // Response class for OrderItem
    public static class OrderItemResponse {

        private Long id;
        private Long orderId;
        private Long userId;
        private Long cakeId;
        private Integer quantity;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getCakeId() {
            return cakeId;
        }

        public void setCakeId(Long cakeId) {
            this.cakeId = cakeId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}