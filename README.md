# 🍰 Cake Delight

> **A cloud-native cake ordering application built using Spring Boot Microservices, React, MySQL, RabbitMQ, Docker, and Kubernetes.**

Cake Delight is a full-stack **microservices-based cake ordering application** that provides user authentication, cake browsing, search and filtering, basket management, checkout, order tracking, ratings, reviews, and asynchronous order notifications.

---

## 📌 Table of Contents

* [Overview](#-overview)
* [Key Features](#-key-features)
* [System Architecture](#️-system-architecture)
* [Microservices](#-microservices)
* [Technology Stack](#️-technology-stack)
* [Authentication](#-authentication)
* [Catalog Service](#-catalog-service)
* [Basket & Order Management](#-basket--order-management)
* [Rating & Review Service](#-rating--review-service)
* [RabbitMQ Event-Driven Communication](#-rabbitmq-event-driven-communication)
* [Database Architecture](#️-database-architecture)
* [React Frontend](#-react-frontend)
* [Docker](#-docker)
* [Kubernetes](#️-kubernetes)
* [End-to-End Flow](#-end-to-end-flow)
* [Project Structure](#-project-structure)
* [API Overview](#-api-overview)
* [Documentation](#-documentation)
* [Setup & Running](#-setup--running)
* [Application Entry Points](#-application-entry-points)
* [Future Enhancements](#-future-enhancements)
* [Project Information](#-project-information)

---

## 📌 Overview

Cake Delight follows a **microservices architecture** where each major business capability is implemented as an independent service.

The application uses:

* **Spring Boot** for backend microservices
* **Spring Cloud Gateway** as the API Gateway
* **React** for the frontend
* **MySQL** using database-per-service architecture
* **RabbitMQ** for asynchronous event-driven communication
* **JWT** for authentication and authorization
* **Docker** for containerization
* **Kubernetes** for container orchestration

---

## ✨ Key Features

### 🔐 Authentication

* User registration
* User login
* JWT-based authentication
* Protected APIs
* User ownership validation

### 🍰 Cake Catalog

* Browse cakes
* Cake details
* Cake search
* Cake filtering
* Cake availability
* Cake images

### 🛒 Basket & Orders

* Add cakes to basket
* View basket
* Update quantities
* Remove items
* Checkout
* Order creation
* Order tracking
* Order cancellation from applicable states

### ⭐ Ratings & Reviews

* Submit ratings
* Submit reviews
* Rating validation
* Purchased-cake verification
* Delivered-order verification
* Duplicate-rating prevention
* Average cake rating

### 🔔 Notifications

* Order completion notifications
* Order status notifications
* Asynchronous message processing
* RabbitMQ-based communication
* Retry configuration

### ☁️ Cloud-Native Architecture

* Independent microservices
* Database-per-service
* API Gateway
* RabbitMQ messaging
* Docker containers
* Kubernetes deployment configuration

---

# 🏗️ System Architecture

```text
                         ┌───────────────────┐
                         │   React Frontend  │
                         │    Port: 5173     │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │    API Gateway    │
                         │    Port: 8084     │
                         └─────────┬─────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
              ▼                    ▼                    ▼
      ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
      │ User Service │     │   Catalog    │     │    Order     │
      │    :8085     │     │   Service    │     │   Service    │
      └──────┬───────┘     │    :8080     │     │    :8081     │
             │             └──────┬───────┘     └──────┬───────┘
             │                    │                    │
             ▼                    ▼                    │
        ┌─────────┐          ┌────────────┐            │
        │ user_db │          │ catalog_db │            │
        └─────────┘          └────────────┘            │
                                                       │
                    ┌──────────────────────────────────┤
                    │                                  │
                    ▼                                  ▼
             ┌──────────────┐                    ┌──────────────┐
             │    Rating    │                    │ Notification │
             │   Service    │                    │   Service    │
             │    :8082     │                    │    :8083     │
             └──────┬───────┘                    └──────┬───────┘
                    │                                  │
                    ▼                                  ▼
              ┌───────────┐                    ┌──────────────────┐
              │ rating_db │                    │ notification_db  │
              └───────────┘                    └──────────────────┘

                              Order Service
                                   │
                                   │ Publish Events
                                   ▼
                              ┌──────────┐
                              │ RabbitMQ │
                              └────┬─────┘
                                   │
                                   │ Consume Events
                                   ▼
                           Notification Service
```

---

# 🧩 Microservices

| Service                  |   Port | Responsibility                                   |
| ------------------------ | -----: | ------------------------------------------------ |
| **User Service**         | `8085` | Registration, login and JWT authentication       |
| **Catalog Service**      | `8080` | Cake catalog, search, filtering and availability |
| **Order Service**        | `8081` | Basket, orders and checkout                      |
| **Rating Service**       | `8082` | Ratings and reviews                              |
| **Notification Service** | `8083` | Order notifications and event processing         |
| **API Gateway**          | `8084` | Central entry point for backend APIs             |

---

# 🔐 Authentication

Cake Delight uses **JWT-based authentication** to secure protected APIs.

### Registration

```http
POST /users/register
```

### Login

```http
POST /users/login
```

After successful login, the server returns a JWT token.

Authenticated requests use:

```http
Authorization: Bearer <JWT_TOKEN>
```

The authenticated user's identity is used to enforce:

* Order ownership
* Basket ownership
* Rating ownership
* Protected operations

### Authentication Flow

```text
User
  │
  ▼
Register
  │
  ▼
Login
  │
  ▼
JWT Token
  │
  ▼
Authorization Header
  │
  ▼
API Gateway
  │
  ▼
Protected Microservice
```

---

# 🍰 Catalog Service

The Catalog Service manages cake-related operations.

### Features

* Cake listing
* Cake details
* Cake search
* Cake filtering
* Cake availability
* Cake images

### Example API

```http
GET /cakes
```

Example:

```http
GET http://localhost:8084/cakes
```

---

# 🛒 Basket & Order Management

The Order Service manages the complete basket and ordering lifecycle.

### Supported Operations

* Add cake to basket
* View basket
* Update quantity
* Remove cake
* Checkout
* Create order
* Track order
* Cancel applicable orders

### Main APIs

```http
POST /orders/basket
GET /orders/basket/{orderId}
POST /orders/checkout/{orderId}
```

### Order Status Flow

```text
┌────────┐
│ PLACED │
└───┬────┘
    │
    ▼
┌───────────┐
│ CONFIRMED │
└─────┬─────┘
      │
      ▼
┌───────────┐
│ PREPARING │
└─────┬─────┘
      │
      ▼
┌─────────────────┐
│ OUT_FOR_DELIVERY│
└────────┬────────┘
         │
         ▼
   ┌───────────┐
   │ DELIVERED │
   └───────────┘
```

Cancellation is supported from applicable order states.

---

# ⭐ Rating & Review Service

The Rating Service allows users to submit ratings and reviews for cakes they have purchased.

Before accepting a rating, the service validates:

* JWT authentication
* User ownership
* Order existence
* Delivered order status
* Purchased cake
* Rating value between `1` and `5`
* Duplicate rating prevention

### Rating APIs

```http
POST /ratings
GET /ratings/cake/{cakeId}
GET /ratings/cake/{cakeId}/average
```

### Example

```http
GET http://localhost:8084/ratings/cake/1
```

---

# 📨 RabbitMQ Event-Driven Communication

RabbitMQ provides asynchronous communication between the **Order Service** and **Notification Service**.

### Exchange

```text
order.exchange
```

### Exchange Type

```text
Topic Exchange
```

### Events

```text
order.completed
order.status.changed
```

### Queues

```text
order.completed.queue
order.status.changed.queue
```

### Event Flow

```text
┌───────────────┐
│ Order Service │
└───────┬───────┘
        │
        │ Publish Event
        ▼
┌────────────────┐
│    RabbitMQ    │
│ order.exchange │
└───────┬────────┘
        │
        │ Consume Event
        ▼
┌──────────────────────┐
│ Notification Service │
└──────────┬───────────┘
           │
           ▼
   ┌──────────────────┐
   │ notification_db  │
   └──────────────────┘
```

The Notification Service includes retry configuration for message processing.

---

# 🗄️ Database Architecture

Cake Delight follows the **Database-per-Service** pattern.

Each microservice owns its own database.

| Service              | Database          |
| -------------------- | ----------------- |
| User Service         | `user_db`         |
| Catalog Service      | `catalog_db`      |
| Order Service        | `order_db`        |
| Rating Service       | `rating_db`       |
| Notification Service | `notification_db` |

### Database Technology

* MySQL
* Spring Data JPA
* Hibernate

### Architecture

```text
User Service
     │
     ▼
 user_db

Catalog Service
     │
     ▼
catalog_db

Order Service
     │
     ▼
 order_db

Rating Service
     │
     ▼
rating_db

Notification Service
     │
     ▼
notification_db
```

This keeps service data ownership independent and reduces direct database coupling between microservices.

---

# 💻 React Frontend

The frontend is developed using **React**.

### Frontend Features

* User registration
* User login
* JWT authentication
* Cake browsing
* Search and filtering
* Basket management
* Checkout
* Order tracking
* Ratings and reviews
* Notification-related UI

### Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

API requests are routed through:

```text
http://localhost:8084
```

---

# 🐳 Docker

Each backend microservice is containerized using Docker.

### Docker Images

```text
cake-api-gateway
cake-catalog-service
cake-order-service
cake-rating-service
cake-notification-service
cake-user-service
```

### Build Example

```bash
docker build -t cake-user-service:latest .
```

### View Images

```bash
docker images
```

### View Running Containers

```bash
docker ps
```

---

# ☸️ Kubernetes

Kubernetes configuration files are available in:

```text
k8s/
```

### Apply Kubernetes Configuration

```bash
kubectl apply -f k8s/
```

### Check Pods

```bash
kubectl get pods
```

### Check Services

```bash
kubectl get services
```

### Check Deployments

```bash
kubectl get deployments
```

### Kubernetes Architecture

```text
                 Kubernetes Cluster
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
   API Gateway      Microservices     Supporting
      Pod              Pods          Components
                                      │
                              ┌───────┴───────┐
                              │               │
                              ▼               ▼
                           MySQL           RabbitMQ
```

---

# 🔄 End-to-End Flow

The complete application flow is:

```text
Register
   ↓
Login
   ↓
JWT Token
   ↓
Browse Cakes
   ↓
Search / Filter
   ↓
Add Cake to Basket
   ↓
View Basket
   ↓
Checkout
   ↓
Order Created
   ↓
Order Confirmed
   ↓
RabbitMQ Event
   ↓
Notification Service
   ↓
Notification Created
   ↓
Order Status Updates
   ↓
Preparing
   ↓
Out For Delivery
   ↓
Delivered
   ↓
Submit Rating / Review
   ↓
View Rating / Average
```

---

# 📁 Project Structure

```text
Cake-Delight/
│
├── api-gateway/
│
├── catalog-service/
│
├── order-service/
│
├── rating-service/
│
├── notification-service/
│
├── user-service/
│
├── frontend/
│
├── k8s/
│
├── README.md
├── API_DOCUMENTATION.md
├── AUTHENTICATION.md
├── DATABASE_SCHEMA.md
├── EVENT_CONTRACTS.md
└── SETUP_AND_E2E.md
```

---

# 🔌 API Overview

| Functionality  | Method | Endpoint                         |
| -------------- | ------ | -------------------------------- |
| Register       | `POST` | `/users/register`                |
| Login          | `POST` | `/users/login`                   |
| Get Cakes      | `GET`  | `/cakes`                         |
| Add to Basket  | `POST` | `/orders/basket`                 |
| View Basket    | `GET`  | `/orders/basket/{orderId}`       |
| Checkout       | `POST` | `/orders/checkout/{orderId}`     |
| Create Rating  | `POST` | `/ratings`                       |
| Cake Ratings   | `GET`  | `/ratings/cake/{cakeId}`         |
| Average Rating | `GET`  | `/ratings/cake/{cakeId}/average` |

> All protected endpoints require a valid JWT token.

---

# 📚 Documentation

Detailed project documentation is available in the following files:

| Document                                       | Description                               |
| ---------------------------------------------- | ----------------------------------------- |
| [`API_DOCUMENTATION.md`](API_DOCUMENTATION.md) | API endpoints and usage                   |
| [`AUTHENTICATION.md`](AUTHENTICATION.md)       | JWT authentication and authorization      |
| [`DATABASE_SCHEMA.md`](DATABASE_SCHEMA.md)     | Database architecture and schemas         |
| [`EVENT_CONTRACTS.md`](EVENT_CONTRACTS.md)     | RabbitMQ events and message contracts     |
| [`SETUP_AND_E2E.md`](SETUP_AND_E2E.md)         | Setup instructions and end-to-end testing |

---

# 🛠️ Technology Stack

| Technology               | Usage                   |
| ------------------------ | ----------------------- |
| **Java**                 | Backend development     |
| **Spring Boot**          | Microservices           |
| **Spring Cloud Gateway** | API Gateway             |
| **Spring Data JPA**      | Data persistence        |
| **Hibernate**            | ORM                     |
| **MySQL**                | Database                |
| **RabbitMQ**             | Asynchronous messaging  |
| **JWT**                  | Authentication          |
| **React**                | Frontend                |
| **Maven**                | Build tool              |
| **Docker**               | Containerization        |
| **Kubernetes**           | Container orchestration |
| **JavaScript**           | Frontend development    |

---

# 🚀 Setup & Running

## Prerequisites

Make sure the following are installed:

```text
Java
Maven
Node.js
npm
MySQL
RabbitMQ
Docker
Kubernetes / kubectl
```

---

## 1️⃣ Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd Cake-Delight
```

---

## 2️⃣ Start MySQL

Create the required databases:

```text
user_db
catalog_db
order_db
rating_db
notification_db
```

Configure the database credentials in the respective Spring Boot services.

---

## 3️⃣ Start RabbitMQ

Make sure RabbitMQ is running before starting the Order and Notification services.

The application uses:

```text
order.exchange
```

for event-driven communication.

---

## 4️⃣ Start Backend Services

Start each Spring Boot service using Maven:

```bash
mvn spring-boot:run
```

Run the services on their configured ports:

```text
Catalog Service       → 8080
Order Service         → 8081
Rating Service        → 8082
Notification Service  → 8083
API Gateway           → 8084
User Service          → 8085
```

---

## 5️⃣ Start React Frontend

```bash
cd frontend
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

---

## 6️⃣ Docker Deployment

Build the required images:

```bash
docker build -t cake-user-service:latest ./user-service
docker build -t cake-catalog-service:latest ./catalog-service
docker build -t cake-order-service:latest ./order-service
docker build -t cake-rating-service:latest ./rating-service
docker build -t cake-notification-service:latest ./notification-service
docker build -t cake-api-gateway:latest ./api-gateway
```

---

## 7️⃣ Kubernetes Deployment

Apply the Kubernetes configuration:

```bash
kubectl apply -f k8s/
```

Verify:

```bash
kubectl get pods
kubectl get services
kubectl get deployments
```

---

# 🌐 Application Entry Points

| Component            | URL                   |
| -------------------- | --------------------- |
| React Frontend       | http://localhost:5173 |
| Catalog Service      | http://localhost:8080 |
| Order Service        | http://localhost:8081 |
| Rating Service       | http://localhost:8082 |
| Notification Service | http://localhost:8083 |
| API Gateway          | http://localhost:8084 |
| User Service         | http://localhost:8085 |

For normal application access, the **API Gateway (`8084`)** acts as the central backend entry point.

---

# 🔭 Future Enhancements

Potential improvements include:

* Online payment integration
* Email/SMS notification integration
* Redis caching
* Service discovery
* Centralized configuration
* Distributed tracing
* Prometheus and Grafana monitoring
* CI/CD pipeline
* Cloud deployment
* API documentation with Swagger/OpenAPI
* Enhanced fault tolerance and circuit breakers

---

# 👨‍💻 Project Information

### 🍰 Cake Delight

**Cloud-Native Microservices Application**

Cake Delight demonstrates a distributed application architecture using:

```text
Spring Boot Microservices
        +
React
        +
REST APIs
        +
JWT Authentication
        +
RabbitMQ
        +
Database-per-Service
        +
Docker
        +
Kubernetes
```

The project demonstrates concepts including **microservices architecture, API Gateway, authentication, asynchronous messaging, database isolation, containerization, and Kubernetes orchestration**.

---

## ⭐ If You Find This Project Useful

Consider giving the repository a ⭐ **star** and exploring the project documentation.

**Built with ☕ Java, 🍃 Spring Boot, ⚛️ React, 🐇 RabbitMQ, 🐬 MySQL, 🐳 Docker, and ☸️ Kubernetes.**
