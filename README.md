\# 🍰 Cake Delight - Microservices Application



Cake Delight is a cloud-native microservices-based application developed using Spring Boot, MySQL, RabbitMQ, Docker, and Kubernetes.



The application manages cakes, orders, ratings, and notifications using independently deployable microservices.



\---



\## 🏗️ Architecture



The application consists of the following components:



\- API Gateway

\- Catalog Service

\- Order Service

\- Rating Service

\- Notification Service

\- RabbitMQ

\- MySQL

\- Docker

\- Kubernetes



\### Request Flow



Client

&#x20;  |

&#x20;  v

API Gateway

&#x20;  |

&#x20;  +--------------------+

&#x20;  |         |          |

&#x20;  v         v          v

Catalog    Order      Rating

Service    Service    Service

&#x20;            |

&#x20;            v

&#x20;         RabbitMQ

&#x20;            |

&#x20;            v

&#x20;      Notification

&#x20;         Service



\---



\## 🚀 Microservices



| Service | Port | Purpose |

|---|---:|---|

| API Gateway | 8084 | Entry point for client requests |

| Catalog Service | 8080 | Manages cake information |

| Order Service | 8081 | Manages customer orders |

| Rating Service | 8082 | Manages cake ratings |

| Notification Service | 8083 | Processes order completion notifications |

| RabbitMQ | 5672 | Message broker |

| RabbitMQ Management | 15672 | RabbitMQ management interface |



\---



\## 🛠️ Technologies Used



\- Java 21

\- Spring Boot

\- Spring Data JPA

\- MySQL

\- RabbitMQ

\- REST APIs

\- Docker

\- Kubernetes

\- Git \& GitHub



\---



\## 📁 Project Structure



Cake-Delight/

│

├── cake-api-gateway/

├── cake-catalog-service/

├── cake-order-service/

├── cake-rating-service/

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




🐳 Docker

Docker is used to containerize the microservices.

Example Docker commands:

docker images
docker ps
docker build -t <image-name> .
docker run -p <host-port>:<container-port> <image-name>



☸️ Kubernetes Deployment

The Kubernetes manifests are located inside:

k8s/

Apply the deployments using:

kubectl apply -f k8s/api-gateway-deployment.yaml
kubectl apply -f k8s/catalog-deployment.yaml
kubectl apply -f k8s/order-deployment.yaml
kubectl apply -f k8s/rating-deployment.yaml
kubectl apply -f k8s/notification-deployment.yaml
kubectl apply -f k8s/rabbitmq.yaml

Check pods:

kubectl get pods

Check services:

kubectl get services

Check deployments:

kubectl get deployments




🐰 RabbitMQ Verification

Check RabbitMQ status:

kubectl exec deployment/rabbitmq -- rabbitmqctl status

Check connections:

kubectl exec deployment/rabbitmq -- rabbitmqctl list_connections

Check queues:

kubectl exec deployment/rabbitmq -- rabbitmqctl list_queues name messages consumers

The application uses:

order.completed.queue

for order completion messages.




🔌 API Endpoints

Catalog Service

Get all cakes:

GET /cakes

Get cake by ID:

GET /cakes/{id}

Order Service

Get all orders:

GET /orders

Get order by ID:

GET /orders/{id}

Create an order:

POST /orders

Add item to basket:

POST /orders/basket

Get basket:

GET /orders/basket/{orderId}

Update basket item:

PUT /orders/basket/item/{itemId}

Delete basket item:

DELETE /orders/basket/item/{itemId}

Checkout order:

POST /orders/checkout/{orderId}
Notification Service

Get notifications:

GET /notifications
Health Checks

API Gateway:

GET /actuator/health

Other services expose their respective Spring Boot health endpoints.



🧪 End-to-End Testing

Example order creation:

curl -X POST http://127.0.0.1:8084/orders ^
-H "Content-Type: application/json" ^
-d "{\"customerName\":\"Phase8 Final Test\",\"cakeId\":2,\"quantity\":1,\"totalPrice\":700}"

Checkout:

curl -X POST http://127.0.0.1:8084/orders/13/checkout

Verify order:

curl http://127.0.0.1:8084/orders/13

Verify notifications:

curl http://127.0.0.1:8084/notifications

Verify RabbitMQ:

kubectl exec deployment/rabbitmq -- rabbitmqctl list_queues name messages consumers



✅ Current Kubernetes Status

All required application components are deployed successfully.

API Gateway — Running
Catalog Service — Running
Order Service — Running
Rating Service — Running
Notification Service — Running
RabbitMQ — Running

RabbitMQ consumer:

order.completed.queue
messages: 0
consumers: 1

This confirms that the Notification Service is consuming messages from RabbitMQ.


🔄 Order Notification Flow


Client creates an order.
Order Service stores the order.
Client checks out the order.
Order status becomes CONFIRMED.
Order Service publishes an event to RabbitMQ.
RabbitMQ places the event in order.completed.queue.
Notification Service consumes the event.
Notification is stored in MySQL.
Notification status becomes SENT.



📊 Example Successful Notification


{
  "id": 8,
  "orderId": 13,
  "customerName": "Phase8 Final Test",
  "message": "Order 13 completed successfully.",
  "status": "SENT"
}



🔍 Kubernetes Validation


All Kubernetes manifests were successfully validated using:

kubectl apply --dry-run=client -f <file>

Validated files:

api-gateway-deployment.yaml
catalog-deployment.yaml
notification-deployment.yaml
order-deployment.yaml
rabbitmq.yaml
rating-deployment.yaml



👨‍💻 Project


Project: Cake Delight Microservices Application

Architecture: Microservices

Containerization: Docker

Orchestration: Kubernetes

Messaging: RabbitMQ

Database: MySQL

Backend: Spring Boot / Java



📌 Conclusion


Cake Delight demonstrates a complete microservices architecture with independent services, REST-based communication, asynchronous messaging using RabbitMQ, containerization using Docker, and deployment using Kubernetes.

