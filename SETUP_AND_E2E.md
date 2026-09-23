# Cake Delight – Setup and End-to-End Demonstration

## 1. Project Overview

Cake Delight is a cloud-native microservices application for online cake ordering.

The application contains:

- User Service
- Catalog Service
- Order Service
- Rating Service
- Notification Service
- API Gateway
- RabbitMQ
- MySQL
- React Frontend
- Docker
- Kubernetes

---

## 2. Technology Stack

| Technology | Purpose |
|---|---|
| Java | Backend development |
| Spring Boot | Microservices |
| Spring Cloud Gateway | API Gateway |
| MySQL | Database |
| Spring Data JPA | Database access |
| RabbitMQ | Asynchronous messaging |
| JWT | Authentication |
| React | Frontend |
| Docker | Containerization |
| Kubernetes | Container orchestration |
| Maven | Build management |

---

## 3. Service Ports

| Service | Port |
|---|---:|
| Catalog Service | 8080 |
| Order Service | 8081 |
| Rating Service | 8082 |
| Notification Service | 8083 |
| API Gateway | 8084 |
| User Service | 8085 |
| RabbitMQ | 5672 |
| Frontend | 5173 |

---

## 4. Database

The application uses MySQL with separate databases for each service.

```text
user_db
catalog_db
order_db
rating_db
notification_db

MySQL runs on:

localhost:3306
5. Start RabbitMQ

RabbitMQ runs on:

localhost:5672

Development credentials:

Username: guest
Password: guest
6. Run Backend Services

Start the following Spring Boot services:

User Service
Catalog Service
Order Service
Rating Service
Notification Service
API Gateway

The API Gateway acts as the main backend entry point.

7. Run Frontend

Open Command Prompt or PowerShell:

cd C:\Microservices\Cake-Delight\frontend

Install dependencies if required:

npm install

Start the frontend:

npm run dev

Frontend:

http://localhost:5173

API Gateway:

http://localhost:8084
8. End-to-End Flow
Step 1 – Register

Register a new user:

POST /users/register
Step 2 – Login

Login using the registered credentials:

POST /users/login

The User Service returns a JWT token.

Step 3 – Browse Cakes

Retrieve the cake catalog:

GET /cakes

Users can search and filter available cakes.

Step 4 – Add Cake to Basket

Add a cake to the basket:

POST /orders/basket

The basket belongs to the authenticated user.

Step 5 – View Basket
GET /orders/basket/{orderId}
Step 6 – Checkout

Checkout the order:

POST /orders/checkout/{orderId}

The Order Service:

Validates the basket.
Retrieves cake information.
Calculates the total price.
Updates the order.
Publishes an order completion event.
Step 7 – RabbitMQ Event

The Order Service publishes:

order.completed

to:

order.exchange

RabbitMQ delivers the event to:

order.completed.queue
Step 8 – Notification

The Notification Service consumes the event and stores a notification in:

notification_db
Step 9 – Order Status

The order follows the supported lifecycle:

CONFIRMED
    |
    v
PREPARING
    |
    v
OUT_FOR_DELIVERY
    |
    v
DELIVERED

Status changes generate RabbitMQ events.

Step 10 – Status Notification

The Notification Service consumes the status event and stores the notification.

Example:

Order status changed from CONFIRMED to PREPARING
Step 11 – Submit Rating

After the order is delivered:

POST /ratings

The Rating Service validates:

JWT authentication
User ownership
Order existence
Delivered status
Purchased cake
Rating value
Duplicate rating
Step 12 – View Rating

Get ratings:

GET /ratings/cake/{cakeId}

Get average rating:

GET /ratings/cake/{cakeId}/average
9. API Gateway Flow
React Frontend
       |
       v
API Gateway :8084
       |
       +----> User Service :8085
       |
       +----> Catalog Service :8080
       |
       +----> Order Service :8081
       |
       +----> Rating Service :8082
       |
       +----> Notification Service :8083
       
       
10. Docker

Each backend service is containerized using Docker.

Docker images:

cake-api-gateway
cake-catalog-service
cake-order-service
cake-rating-service
cake-notification-service
cake-user-service

Example:

docker build -t cake-user-service:latest .


11. Kubernetes

Kubernetes configuration files are located in:

k8s/

The project contains deployments and services for the microservices and RabbitMQ.

Check pods:

kubectl get pods

Check services:

kubectl get services

Check deployments:

kubectl get deployments

Apply Kubernetes configuration:

kubectl apply -f k8s/

12. E2E Demonstration Sequence
Register
   ↓
Login
   ↓
JWT Token
   ↓
Browse Cakes
   ↓
Add Cake to Basket
   ↓
View Basket
   ↓
Checkout
   ↓
Order Confirmed
   ↓
RabbitMQ Event
   ↓
Notification Created
   ↓
Preparing
   ↓
Out for Delivery
   ↓
Delivered
   ↓
Status Notification
   ↓
Submit Rating
   ↓
View Rating / Average
13. Project Entry Points

Frontend:

http://localhost:5173

API Gateway:

http://localhost:8084