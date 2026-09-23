# Cake Delight – Authentication Documentation

## 1. Overview

Cake Delight uses JWT-based authentication for user authentication and authorization.

The User Service handles user registration and login. After successful login, the user receives a JWT token.

The token is used when accessing protected APIs.

---

## 2. User Registration

### Endpoint

```text
POST /users/register
```

### Gateway URL

```text
http://localhost:8084/users/register
```

### Example Request

```json
{
  "name": "Vikas",
  "email": "vikas@example.com",
  "password": "Vikas@123"
}
```

The User Service stores the registered user's information in the User Service database.

---

## 3. User Login

### Endpoint

```text
POST /users/login
```

### Gateway URL

```text
http://localhost:8084/users/login
```

### Example Request

```json
{
  "email": "vikas@example.com",
  "password": "Vikas@123"
}
```

After successful authentication, the User Service generates a JWT token.

---

## 4. JWT Authentication

Protected requests use the following HTTP header:

```text
Authorization: Bearer <JWT_TOKEN>
```

Example:

```text
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

The JWT contains user identity information used by the backend services.

---

## 5. User Identification

The JWT contains the authenticated user's `userId`.

The Order Service uses this information to associate orders and basket items with the authenticated user.

The Rating Service also uses the authenticated `userId` to verify ownership before accepting a rating.

---

## 6. Authorization

The application uses the authenticated user's identity to enforce ownership rules.

Examples:

- A user can access their own orders.
- A user can manage their own basket.
- A user can only rate a cake that they purchased.
- A user can only rate after the order has been delivered.
- Duplicate ratings for the same user, order, and cake are prevented.

---

## 7. Rating Security Validation

Before a rating is accepted, the Rating Service validates:

1. JWT token is present.
2. JWT token is valid.
3. User ID is extracted from the JWT.
4. Order exists.
5. Order belongs to the authenticated user.
6. Order has been delivered.
7. The cake exists in the purchased basket/order.
8. Rating value is between 1 and 5.
9. The user has not already rated the same cake for the same order.

---

## 8. Authentication Flow

```text
User
  |
  | Register
  ↓
User Service
  |
  | Login
  ↓
User Service
  |
  | JWT Token
  ↓
Frontend
  |
  | Authorization: Bearer <JWT>
  ↓
API Gateway
  |
  +------> Order Service
  |
  +------> Rating Service
```

---

## 9. Security Summary

| Feature | Implementation |
|---|---|
| User Registration | User Service |
| User Login | User Service |
| Authentication | JWT |
| User Identification | JWT `userId` claim |
| Order Ownership | User ID validation |
| Basket Ownership | User ID validation |
| Rating Authorization | Order and user validation |
| Duplicate Rating Prevention | User + Order + Cake validation |

---

## 10. Token Secret

The JWT signing secret is configured internally in the User Service and related services.

**Production Note:** Secrets should be stored using environment variables or Kubernetes Secrets rather than being hard-coded in source code.