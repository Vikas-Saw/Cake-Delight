# Cake Delight – Database Schema

## Database Architecture

Cake Delight uses a database-per-service architecture. Each microservice manages its own MySQL database.

| Service | Database |
|---|---|
| User Service | user_db |
| Catalog Service | catalog_db |
| Order Service | order_db |
| Rating Service | rating_db |
| Notification Service | notification_db |

## Catalog Database

**Database:** `catalog_db`  
**Table:** `cakes`

| Column | Description |
|---|---|
| id | Cake ID |
| name | Cake name |
| price | Cake price |
| category | Cake category |
| description | Cake description |
| available | Availability |
| image_reference | Image reference |

## Order Database

**Database:** `order_db`

### orders

| Column | Description |
|---|---|
| id | Order ID |
| customerName | Customer name |
| userId | User ID |
| cakeId | Cake ID |
| quantity | Quantity |
| totalPrice | Total price |
| status | Order status |

### order_items

| Column | Description |
|---|---|
| id | Item ID |
| orderId | Order ID |
| userId | User ID |
| cakeId | Cake ID |
| quantity | Quantity |

## Rating Database

**Database:** `rating_db`  
**Table:** `ratings`

| Column | Description |
|---|---|
| id | Rating ID |
| userId | User ID |
| orderId | Order ID |
| cakeId | Cake ID |
| customerName | Customer name |
| rating | Rating (1–5) |
| review | Customer review |

## Notification Database

**Database:** `notification_db`  
**Table:** `notification`

| Column | Description |
|---|---|
| id | Notification ID |
| orderId | Order ID |
| customerName | Customer name |
| message | Notification message |
| status | Notification status |
| attemptCount | Delivery attempt count |

## Database Ownership

```text
User Service ----------> user_db
Catalog Service -------> catalog_db
Order Service ---------> order_db
Rating Service --------> rating_db
Notification Service --> notification_db