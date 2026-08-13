# 🍰 Cake Delight - Microservices Application

Cake Delight is a **cloud-native microservices-based application** developed using **Spring Boot, MySQL, RabbitMQ, Docker, and Kubernetes**.

The application manages cakes, customer orders, ratings, and notifications using independently deployable microservices.

---

## 📑 Table of Contents

1. [Architecture](#-architecture)
2. [Microservices](#-microservices)
3. [Technologies Used](#-technologies-used)
4. [Project Structure](#-project-structure)
5. [Docker](#-docker)
6. [Kubernetes Deployment](#-kubernetes-deployment)
7. [RabbitMQ Verification](#-rabbitmq-verification)
8. [API Endpoints](#-api-endpoints)
9. [Health Checks](#-health-checks)
10. [End-to-End Testing](#-end-to-end-testing)
11. [Order Notification Flow](#-order-notification-flow)
12. [Successful Notification Example](#-successful-notification-example)
13. [Kubernetes Validation](#-kubernetes-validation)
14. [Project Summary](#-project-summary)
15. [Conclusion](#-conclusion)

---

## 🏗️ Architecture

The Cake Delight application consists of the following components:

* **API Gateway**
* **Catalog Service**
* **Order Service**
* **Rating Service**
* **Notification Service**
* **RabbitMQ**
* **MySQL**
* **Docker**
* **Kubernetes**

### Request Flow

```text
                    Client
                       |
                       v
                 API Gateway
                       |
          +------------+------------+
          |            |            |
          v            v            v
      Catalog        Order        Rating
      Service       Service       Service
                       |
                       v
                   RabbitMQ
                       |
                       v
              Notification Service
                       |
                       v
                     MySQL
```

---

## 🚀 Microservices

| Service              |  Port | Purpose                                  |
| -------------------- | ----: | ---------------------------------------- |
| API Gateway          |  8084 | Entry point for client requests          |
| Catalog Service      |  8080 | Manages cake information                 |
| Order Service        |  8081 | Manages customer orders                  |
| Rating Service       |  8082 | Manages cake ratings                     |
| Notification Service |  8083 | Processes order completion notifications |
| RabbitMQ             |  5672 | Message broker                           |
| RabbitMQ Management  | 15672 | RabbitMQ management interface            |

---

## 🛠️ Technologies Used

* **Java 21**
* **Spring Boot**
* **Spring Data JPA**
* **MySQL**
* **RabbitMQ**
* **REST APIs**
* **Docker**
* **Kubernetes**
* **Git & GitHub**

---

## 📁 Project Structure

```text
Cake-Delight/
│
├── cake-api-gateway/
│
├── cake-catalog-service/
│
├── cake-order-service/
│
├── cake-rating-service/
│
├── cake-notification-service/
│
├── k8s/
│   ├── api-gateway-deployment.yaml
│   ├── catalog-deployment.yaml
│   ├── notification-deployment.yaml
│   ├── order-deployment.yaml
│   ├── rabbitmq.yaml
│   └── rating-deployment.yaml
│
└── README.md
```

---

# 🐳 Docker

Docker is used to containerize the Cake Delight microservices.

### Common Docker Commands

```bash
docker images
docker ps
docker build -t <image-name> .
docker run -p <host-port>:<container-port> <image-name>
```

Docker provides consistent and isolated environments for running each microservice.

---

# ☸️ Kubernetes Deployment

The Kubernetes deployment manifests are located inside:

```text
k8s/
```

### Apply Kubernetes Deployments

```bash
kubectl apply -f k8s/api-gateway-deployment.yaml
kubectl apply -f k8s/catalog-deployment.yaml
kubectl apply -f k8s/order-deployment.yaml
kubectl apply -f k8s/rating-deployment.yaml
kubectl apply -f k8s/notification-deployment.yaml
kubectl apply -f k8s/rabbitmq.yaml
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

---

# 🐰 RabbitMQ Verification

RabbitMQ is used for **asynchronous communication** between the Order Service and Notification Service.

### Check RabbitMQ Status

```bash
kubectl exec deployment/rabbitmq -- rabbitmqctl status
```

### Check Connections

```bash
kubectl exec deployment/rabbitmq -- rabbitmqctl list_connections
```

### Check Queues

```bash
kubectl exec deployment/rabbitmq -- rabbitmqctl list_queues name messages consumers
```

The application uses the following queue:

```text
order.completed.queue
```

---

# 🔌 API Endpoints

## Catalog Service

### Get All Cakes

```http
GET /cakes
```

### Get Cake by ID

```http
GET /cakes/{id}
```

---

## Order Service

### Get All Orders

```http
GET /orders
```

### Get Order by ID

```http
GET /orders/{id}
```

### Create an Order

```http
POST /orders
```

### Add Item to Basket

```http
POST /orders/basket
```

### Get Basket

```http
GET /orders/basket/{orderId}
```

### Update Basket Item

```http
PUT /orders/basket/item/{itemId}
```

### Delete Basket Item

```http
DELETE /orders/basket/item/{itemId}
```

### Checkout Order

```http
POST /orders/checkout/{orderId}
```

---

## Notification Service

### Get Notifications

```http
GET /notifications
```

---

# ❤️ Health Checks

### API Gateway

```http
GET /actuator/health
```

Other microservices expose their respective Spring Boot Actuator health endpoints.

---

# 🧪 End-to-End Testing

The complete application flow can be tested through the API Gateway.

## 1. Create an Order

```bash
curl -X POST http://127.0.0.1:8084/orders ^
-H "Content-Type: application/json" ^
-d "{\"customerName\":\"Phase8 Final Test\",\"cakeId\":2,\"quantity\":1,\"totalPrice\":700}"
```

## 2. Checkout the Order

```bash
curl -X POST http://127.0.0.1:8084/orders/13/checkout
```

## 3. Verify the Order

```bash
curl http://127.0.0.1:8084/orders/13
```

## 4. Verify Notifications

```bash
curl http://127.0.0.1:8084/notifications
```

## 5. Verify RabbitMQ

```bash
kubectl exec deployment/rabbitmq -- rabbitmqctl list_queues name messages consumers
```

---

# 🔄 Order Notification Flow

The order completion process works as follows:

```text
1. Client creates an order
             ↓
2. Order Service stores the order
             ↓
3. Client checks out the order
             ↓
4. Order status becomes CONFIRMED
             ↓
5. Order Service publishes an event
             ↓
6. RabbitMQ receives the event
             ↓
7. Event enters order.completed.queue
             ↓
8. Notification Service consumes the event
             ↓
9. Notification is stored in MySQL
             ↓
10. Notification status becomes SENT
```

This demonstrates **asynchronous event-driven communication** between microservices.

---

# 📊 Successful Notification Example

```json
{
  "id": 8,
  "orderId": 13,
  "customerName": "Phase8 Final Test",
  "message": "Order 13 completed successfully.",
  "status": "SENT"
}
```

---

# 🔍 Kubernetes Validation

All Kubernetes manifests were successfully validated using:

```bash
kubectl apply --dry-run=client -f <file>
```

### Validated Files

```text
api-gateway-deployment.yaml
catalog-deployment.yaml
notification-deployment.yaml
order-deployment.yaml
rabbitmq.yaml
rating-deployment.yaml
```

---

# ✅ Current Kubernetes Status

All required application components are deployed successfully.

| Component            | Status    |
| -------------------- | --------- |
| API Gateway          | ✅ Running |
| Catalog Service      | ✅ Running |
| Order Service        | ✅ Running |
| Rating Service       | ✅ Running |
| Notification Service | ✅ Running |
| RabbitMQ             | ✅ Running |

### RabbitMQ Consumer Status

```text
Queue: order.completed.queue
Messages: 0
Consumers: 1
```

The presence of **1 consumer** confirms that the **Notification Service is successfully consuming messages from RabbitMQ**.

---

# 📌 Project Summary

| Category         | Technology                             |
| ---------------- | -------------------------------------- |
| Project          | Cake Delight Microservices Application |
| Architecture     | Microservices                          |
| Backend          | Spring Boot / Java 21                  |
| Database         | MySQL                                  |
| Communication    | REST APIs                              |
| Messaging        | RabbitMQ                               |
| Containerization | Docker                                 |
| Orchestration    | Kubernetes                             |
| Version Control  | Git & GitHub                           |

---

# 🎯 Key Features

* Independent microservices architecture
* REST-based communication
* Cake catalog management
* Order and basket management
* Cake rating management
* Order checkout functionality
* Asynchronous order notifications
* RabbitMQ event-driven communication
* MySQL persistence
* Docker containerization
* Kubernetes deployment
* Kubernetes health and deployment validation
* API Gateway for centralized request routing

---

# 📌 Conclusion

Cake Delight demonstrates a complete **cloud-native microservices architecture** using Spring Boot and Java 21.

The application implements independent services for catalog, orders, ratings, and notifications. **RabbitMQ** provides asynchronous event-driven communication, while **MySQL** handles persistent data storage. The services are containerized using **Docker** and deployed using **Kubernetes**.

The successful Kubernetes deployment, RabbitMQ consumer verification, end-to-end order testing, and notification flow demonstrate that the complete Cake Delight microservices application is functioning as intended.
