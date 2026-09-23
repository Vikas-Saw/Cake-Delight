package com.cakedelight.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cakedelight.order.entity.Order;
import com.cakedelight.order.entity.OrderItem;
import com.cakedelight.order.security.JwtService;
import com.cakedelight.order.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtService jwtService;

    public OrderController(
            OrderService orderService,
            JwtService jwtService) {

        this.orderService = orderService;
        this.jwtService = jwtService;
    }

    // =========================================================
    // CREATE ORDER
    // =========================================================

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Order order) {

        try {

            Long userId = extractUserId(authorization);

            Order savedOrder =
                    orderService.createOrder(order, userId);

            return ResponseEntity.ok(savedOrder);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    // =========================================================
    // GET ALL ORDERS OF LOGGED-IN USER
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(
            @RequestHeader("Authorization") String authorization) {

        try {

            Long userId = extractUserId(authorization);

            return ResponseEntity.ok(
                    orderService.getAllOrders(userId)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    // =========================================================
    // GET ORDER BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {

        try {

            Long userId = extractUserId(authorization);

            Order order =
                    orderService.getOrderById(id, userId);

            if (order == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(order);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    // =========================================================
    // UPDATE ORDER
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody Order order) {

        try {

            Long userId = extractUserId(authorization);

            return ResponseEntity.ok(
                    orderService.updateOrder(
                            id,
                            order,
                            userId
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    // =========================================================
    // DELETE ORDER
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {

        try {

            Long userId = extractUserId(authorization);

            orderService.deleteOrder(id, userId);

            return ResponseEntity.ok(
                    "Order deleted successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Order not found or unauthorized");
        }
    }

    // =========================================================
    // ADD ITEM TO BASKET
    // =========================================================

    @PostMapping("/basket")
    public ResponseEntity<OrderItem> addToBasket(
            @RequestHeader("Authorization") String authorization,
            @RequestBody OrderItem item) {

        try {

            Long userId = extractUserId(authorization);

            OrderItem savedItem =
                    orderService.addToBasket(
                            item,
                            userId
                    );

            return ResponseEntity.ok(savedItem);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    // =========================================================
    // VIEW BASKET
    // =========================================================

    @GetMapping("/basket/{orderId}")
    public ResponseEntity<List<OrderItem>> getBasket(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long orderId) {

        try {

            Long userId = extractUserId(authorization);

            return ResponseEntity.ok(
                    orderService.getBasket(
                            orderId,
                            userId
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    // =========================================================
    // UPDATE BASKET ITEM
    // =========================================================

    @PutMapping("/basket/item/{itemId}")
    public ResponseEntity<OrderItem> updateBasketItem(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long itemId,
            @RequestBody OrderItem item) {

        try {

            Long userId = extractUserId(authorization);

            return ResponseEntity.ok(
                    orderService.updateBasketItem(
                            itemId,
                            item,
                            userId
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    // =========================================================
    // REMOVE BASKET ITEM
    // =========================================================

    @DeleteMapping("/basket/item/{itemId}")
    public ResponseEntity<String> removeFromBasket(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long itemId) {

        try {

            Long userId = extractUserId(authorization);

            orderService.removeFromBasket(
                    itemId,
                    userId
            );

            return ResponseEntity.ok(
                    "Basket item removed successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Basket item not found or unauthorized");
        }
    }
    
    
    
		 // =========================================================
		 // UPDATE ORDER STATUS
		 // =========================================================
		
		 @PutMapping("/{id}/status")
		 public ResponseEntity<Order> updateOrderStatus(
		         @RequestHeader("Authorization") String authorization,
		         @PathVariable Long id,
		         @RequestParam String status) {
		
		     try {
		
		         Long userId = extractUserId(authorization);
		
		         return ResponseEntity.ok(
		                 orderService.updateOrderStatus(
		                         id,
		                         status,
		                         userId
		                 )
		         );
		
		     } catch (RuntimeException e) {
		
		         return ResponseEntity
		                 .status(HttpStatus.BAD_REQUEST)
		                 .build();
		     }
		 }

    // =========================================================
    // CHECKOUT
    // =========================================================

    @PostMapping("/checkout/{orderId}")
    public ResponseEntity<Order> checkout(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long orderId) {

        try {

            Long userId = extractUserId(authorization);

            return ResponseEntity.ok(
                    orderService.checkout(
                            orderId,
                            userId
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    // =========================================================
    // EXTRACT USER ID FROM JWT
    // =========================================================

    private Long extractUserId(String authorization) {

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Missing JWT token"
            );
        }

        String token = authorization.substring(7);

        if (!jwtService.isValidToken(token)) {

            throw new RuntimeException(
                    "Invalid JWT token"
            );
        }

        return jwtService.extractUserId(token);
    }
}