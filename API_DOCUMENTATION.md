# Cake Delight – API Documentation

## 1. Overview

Cake Delight is a cloud-native microservices application built using Spring Boot, REST APIs, RabbitMQ, Docker, Kubernetes, and React.

The API Gateway provides a single entry point for the backend services.

### API Gateway

**Base URL:**

`http://localhost:8084`

| Service | Gateway Path | Service Port |
|---|---|---:|
| User Service | `/users/**` | 8085 |
| Catalog Service | `/cakes/**` | 8080 |
| Order Service | `/orders/**` | 8081 |
| Rating Service | `/ratings/**` | 8082 |
| Notification Service | `/notifications/**` | 8083 |

---

# 2. User Service

**Base Path:** `/users`

## Register User

**POST** `/users/register`

Registers a new user.

### Request Body

```json
{
  "name": "Vikas",
  "email": "vikas@example.com",
  "password": "Vikas@123"
}
```

## Login

**POST** `/users/login`

Authenticates a user.

### Request Body

```json
{
  "email": "vikas@example.com",
  "password": "Vikas@123"
}
```

The login process returns JWT authentication information used for protected APIs.

---

# 3. Catalog Service

**Base Path:** `/cakes`

## Get All Cakes

**GET** `/cakes`

Returns the available cakes from the catalog.

### Gateway Example

`GET http://localhost:8084/cakes`

## Get Cake by ID

**GET** `/cakes/{id}`

Returns a specific cake.

### Example

`GET http://localhost:8084/cakes/2`

## Create Cake

**POST** `/cakes`

Creates a new cake.

### Example Request

```json
{
  "name": "Chocolate Truffle Cake",
  "category": "Chocolate",
  "price": 850,
  "description": "Rich chocolate cake",
  "available": true,
  "imageReference": "chocolate-truffle.jpeg"
}
```

## Update Cake

**PUT** `/cakes/{id}`

Updates an existing cake.

## Delete Cake

**DELETE** `/cakes/{id}`

Deletes a cake.

---

# 4. Order Service

**Base Path:** `/orders`

Order APIs use JWT authentication.

### Authentication Header

```text
Authorization: Bearer <JWT_TOKEN>
```

## Create Order

**POST** `/orders`

Creates an order for the authenticated user.

## Get User Orders

**GET** `/orders`

Returns orders belonging to the authenticated user.

## Get Order by ID

**GET** `/orders/{id}`

Returns a specific order belonging to the authenticated user.

## Update Order

**PUT** `/orders/{id}`

Updates an order.

## Delete Order

**DELETE** `/orders/{id}`

Deletes an order.

---

# 5. Basket APIs

## Add Item to Basket

**POST** `/orders/basket`

Adds a cake to the user's basket.

### Example Request

```json
{
  "orderId": 30,
  "cakeId": 2,
  "quantity": 1
}
```

## View Basket

**GET** `/orders/basket/{orderId}`

Returns the basket items for an order.

## Update Basket Item

**PUT** `/orders/basket/item/{itemId}`

Updates the quantity of a basket item.

## Remove Basket Item

**DELETE** `/orders/basket/item/{itemId}`

Removes an item from the basket.

---

# 6. Checkout

## Checkout Order

**POST** `/orders/checkout/{orderId}`

Checks out the order.

The checkout process:

1. Validates the basket.
2. Retrieves cake prices.
3. Calculates the total amount.
4. Updates the order status.
5. Publishes an order completion event through RabbitMQ.

### Example

`POST http://localhost:8084/orders/checkout/30`

---

# 7. Order Status

## Update Order Status

**PUT** `/orders/{id}/status?status={status}`

Updates the status of an order.

### Example

```text
PUT /orders/30/status?status=PREPARING
```

### Order Lifecycle

```text
PLACED
   ↓
CONFIRMED
   ↓
PREPARING
   ↓
OUT_FOR_DELIVERY
   ↓
DELIVERED
```

Cancellation is supported from applicable earlier states.

Order status changes generate RabbitMQ events for the Notification Service.

---

# 8. Rating Service

**Base Path:** `/ratings`

Rating APIs use JWT authentication.

## Submit Rating

**POST** `/ratings`

Allows a user to submit a rating after purchasing and receiving a cake.

### Example Request

```json
{
  "orderId": 30,
  "cakeId": 2,
  "customerName": "Vikas",
  "rating": 5,
  "review": "Excellent cake!"
}
```

The service validates:

- JWT authentication
- Order ownership
- Order delivery status
- Whether the cake was purchased
- Rating value between 1 and 5
- Duplicate ratings

## Get All Ratings

**GET** `/ratings`

Returns ratings.

## Get Ratings for Cake

**GET** `/ratings/cake/{cakeId}`

Returns ratings for a specific cake.

### Example

`GET http://localhost:8084/ratings/cake/2`

## Get Average Rating

**GET** `/ratings/cake/{cakeId}/average`

Returns the average rating for a cake.

### Example

`GET http://localhost:8084/ratings/cake/2/average`

---

# 9. Notification Service

**Base Path:** `/notifications`

The Notification Service consumes order events from RabbitMQ and stores notifications.

## Get All Notifications

**GET** `/notifications`

Returns all stored notifications.

## Get Notification by ID

**GET** `/notifications/{id}`

Returns a specific notification.

## Get Notifications for Order

**GET** `/notifications/order/{orderId}`

Returns notifications related to a specific order.

### Example

`GET http://localhost:8084/notifications/order/30`

Notifications are generated for:

- Order completion
- Order status changes

---

# 10. Authentication

Protected APIs use JWT authentication.

### Authorization Header

```text
Authorization: Bearer <JWT_TOKEN>
```

The JWT contains the authenticated user's identity information.

The Order and Rating services use the authenticated user information to enforce user ownership and authorization.

---

# 11. RabbitMQ Events

Cake Delight uses RabbitMQ for asynchronous communication between the Order Service and Notification Service.

## RabbitMQ Exchange

```text
order.exchange
```

## Order Completion Event

**Routing Key:**

```text
order.completed
```

**Queue:**

```text
order.completed.queue
```

The Order Service publishes this event after successful checkout/order completion.

The Notification Service consumes the event and creates a notification.

## Order Status Change Event

**Routing Key:**

```text
order.status.changed
```

**Queue:**

```text
order.status.changed.queue
```

The Order Service publishes this event when an order status changes.

The Notification Service consumes the event and creates a status notification.

---

# 12. End-to-End Application Flow

```text
User Registration/Login
        ↓
API Gateway
        ↓
Catalog Service
        ↓
Browse/Search Cakes
        ↓
Add Cake to Basket
        ↓
Checkout
        ↓
Order Service
        ↓
RabbitMQ
        ↓
Notification Service
        ↓
Order Status Updates
        ↓
Delivered
        ↓
Rating Service
        ↓
Submit Rating
```

---

# 13. Common Gateway APIs

| Operation | Method | Gateway Endpoint |
|---|---|---|
| Register User | POST | `/users/register` |
| Login | POST | `/users/login` |
| Get Cakes | GET | `/cakes` |
| Get Cake | GET | `/cakes/{id}` |
| Create Cake | POST | `/cakes` |
| Update Cake | PUT | `/cakes/{id}` |
| Delete Cake | DELETE | `/cakes/{id}` |
| Create Order | POST | `/orders` |
| Get Orders | GET | `/orders` |
| Get Order | GET | `/orders/{id}` |
| Add Basket Item | POST | `/orders/basket` |
| View Basket | GET | `/orders/basket/{orderId}` |
| Update Basket Item | PUT | `/orders/basket/item/{itemId}` |
| Remove Basket Item | DELETE | `/orders/basket/item/{itemId}` |
| Checkout | POST | `/orders/checkout/{orderId}` |
| Update Order Status | PUT | `/orders/{id}/status?status=PREPARING` |
| Submit Rating | POST | `/ratings` |
| Get Ratings | GET | `/ratings` |
| Get Cake Ratings | GET | `/ratings/cake/{cakeId}` |
| Get Average Rating | GET | `/ratings/cake/{cakeId}/average` |
| Get Notifications | GET | `/notifications` |
| Get Order Notifications | GET | `/notifications/order/{orderId}` |

---

# 14. Service Ports

| Component | Port |
|---|---:|
| API Gateway | 8084 |
| Catalog Service | 8080 |
| Order Service | 8081 |
| Rating Service | 8082 |
| Notification Service | 8083 |
| User Service | 8085 |
| RabbitMQ | 5672 |

---

## 15. Technology Stack

- Java
- Spring Boot
- Spring Cloud Gateway
- Spring Data JPA
- MySQL
- RabbitMQ
- JWT Authentication
- React
- Docker
- Kubernetes