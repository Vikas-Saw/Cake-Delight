# 🍰 Cake Delight

A cloud-native cake ordering application built using a **Spring Boot microservices architecture** with **React, MySQL, RabbitMQ, Docker, and Kubernetes**.

The application supports user authentication, cake catalog management, basket operations, checkout, order tracking, ratings, and asynchronous notifications.

---

## 📌 Project Overview

Cake Delight is designed as a distributed microservices application where each major business function is handled by an independent service.

### Core Features

- 🔐 JWT-based user authentication
- 🍰 Cake catalog and availability
- 🔎 Cake search and filtering
- 🛒 Basket management
- 💳 Order checkout
- 📦 Order status tracking
- ⭐ Ratings and reviews
- 🔔 Asynchronous order notifications
- 📨 RabbitMQ event-driven communication
- 🗄️ Database-per-service architecture
- 🐳 Docker containerization
- ☸️ Kubernetes deployment
- 💻 React frontend
- 🚪 API Gateway

---

## 🏗️ Architecture

```text
                         React Frontend
                              |
                              v
                       API Gateway :8084
                              |
          +-------------------+-------------------+
          |          |          |          |       |
          v          v          v          v       v
       User       Catalog     Order      Rating  Notification
      :8085        :8080      :8081      :8082     :8083
          |           |          |           |
          v           v          v           v
       user_db    catalog_db  order_db   rating_db
                              |
                              v
                          RabbitMQ
                              |
                              v
                     Notification Service
                              |
                              v
                       notification_db
🧩 Microservices
Service	Port	Responsibility
User Service	8085	Registration, login and JWT authentication
Catalog Service	8080	Cake catalog and availability
Order Service	8081	Orders, basket and checkout
Rating Service	8082	Ratings and reviews
Notification Service	8083	Order notifications
API Gateway	8084	Central API entry point
Supporting Components
MySQL
RabbitMQ
Docker
Kubernetes
React
🔐 Authentication

Cake Delight uses JWT-based authentication.

Registration
POST /users/register
Login
POST /users/login

Authenticated APIs use:

Authorization: Bearer <JWT_TOKEN>

The authenticated user's identity is used to enforce order, basket, and rating ownership.

🍰 Catalog

The Catalog Service provides:

Cake listing
Cake search
Cake filtering
Cake availability
Cake details
Cake images

Example:

GET /cakes
🛒 Basket & Orders

Users can:

Add cakes to the basket
View the basket
Update quantities
Remove items
Checkout
Track order status

Example:

POST /orders/basket
GET /orders/basket/{orderId}
POST /orders/checkout/{orderId}
Order Status Flow
PLACED
   |
   v
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

Cancellation is supported from applicable order states.

⭐ Ratings

Users can submit a rating after their order has been delivered.

The Rating Service validates:

JWT authentication
User ownership
Order existence
Delivered order status
Purchased cake
Rating value between 1 and 5
Duplicate rating prevention

Examples:

POST /ratings
GET /ratings/cake/{cakeId}
GET /ratings/cake/{cakeId}/average
📨 RabbitMQ Event-Driven Communication

RabbitMQ is used for asynchronous communication between the Order Service and Notification Service.

Exchange
order.exchange

Type: Topic

Events
order.completed
order.status.changed
Queues
order.completed.queue
order.status.changed.queue
Event Flow
Order Service
     |
     | Publish Event
     v
RabbitMQ
     |
     v
Notification Service
     |
     v
notification_db

The Notification Service uses retry configuration for message processing.

🗄️ Database Architecture

Cake Delight follows a database-per-service approach.

Service	Database
User Service	user_db
Catalog Service	catalog_db
Order Service	order_db
Rating Service	rating_db
Notification Service	notification_db
Database Technology
MySQL
Spring Data JPA
Hibernate
💻 Frontend

The frontend is developed using React.

Run Frontend
cd frontend
npm install
npm run dev

Frontend:

http://localhost:5173

API Gateway:

http://localhost:8084
🐳 Docker

Each backend microservice is containerized using Docker.

Docker Images
cake-api-gateway
cake-catalog-service
cake-order-service
cake-rating-service
cake-notification-service
cake-user-service

Example:

docker build -t cake-user-service:latest .
☸️ Kubernetes

Kubernetes configuration files are available in:

k8s/
Check Pods
kubectl get pods
Check Services
kubectl get services
Check Deployments
kubectl get deployments
Apply Kubernetes Configuration
kubectl apply -f k8s/
🔄 End-to-End Application Flow
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
Order Status Updates
   ↓
Delivered
   ↓
Submit Rating
   ↓
View Rating / Average
📁 Project Structure
Cake-Delight/
│
├── api-gateway/
├── catalog-service/
├── order-service/
├── rating-service/
├── notification-service/
├── user-service/
├── frontend/
├── k8s/
│
├── README.md
├── API_DOCUMENTATION.md
├── AUTHENTICATION.md
├── DATABASE_SCHEMA.md
├── EVENT_CONTRACTS.md
└── SETUP_AND_E2E.md
📚 Documentation
Document	Description
API_DOCUMENTATION.md	API endpoints and usage
AUTHENTICATION.md	JWT authentication
DATABASE_SCHEMA.md	Database architecture and schema
EVENT_CONTRACTS.md	RabbitMQ events and message contracts
SETUP_AND_E2E.md	Setup and end-to-end flow
🛠️ Technology Stack
Technology	Usage
Java	Backend
Spring Boot	Microservices
Spring Cloud Gateway	API Gateway
Spring Data JPA	Persistence
MySQL	Database
RabbitMQ	Messaging
JWT	Authentication
React	Frontend
Maven	Build Tool
Docker	Containerization
Kubernetes	Orchestration
🚀 Project Entry Points

Frontend

http://localhost:5173

API Gateway

http://localhost:8084
👨‍💻 Project

Cake Delight – Cloud-Native Microservices Application

Built using a microservices architecture with REST APIs, asynchronous messaging, containerization, and Kubernetes orchestration.
