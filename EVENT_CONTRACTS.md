# Cake Delight – Event Contracts

## RabbitMQ

Cake Delight uses RabbitMQ for asynchronous communication between the Order Service and Notification Service.

### Exchange

**Name:** `order.exchange`  
**Type:** Topic Exchange

---

## Order Completed Event

**Producer:** Order Service  
**Consumer:** Notification Service

**Routing Key:** `order.completed`

**Queue:** `order.completed.queue`

### Example Event

```json
{
  "orderId": 30,
  "customerName": "Vikas"
}

This event is published after successful checkout. The Notification Service consumes the event and creates a notification.

Order Status Changed Event

Producer: Order Service
Consumer: Notification Service

Routing Key: order.status.changed

Queue: order.status.changed.queue

Example Event
{
  "orderId": 30,
  "customerName": "Vikas",
  "oldStatus": "CONFIRMED",
  "newStatus": "PREPARING"
}

This event is published whenever the order status changes.

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

Cancellation is supported from PLACED and CONFIRMED.

RabbitMQ Retry

The Notification Service uses message retry configuration.

Maximum attempts: 3
Initial retry interval: 1000 ms
Retry multiplier: 2
Maximum interval: 10000 ms
Failed messages are not automatically requeued
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

Event Summary

| Component             | Value                        |
| --------------------- | ---------------------------- |
| Exchange              | `order.exchange`             |
| Type                  | Topic                        |
| Completed Queue       | `order.completed.queue`      |
| Status Queue          | `order.status.changed.queue` |
| Completed Routing Key | `order.completed`            |
| Status Routing Key    | `order.status.changed`       |
| Producer              | Order Service                |
| Consumer              | Notification Service         |
| Message Format        | JSON                         |
