# Validation and Exception Handling

## Overview

The Bhuk system implements comprehensive input validation and exception handling using Spring Boot's Bean Validation and `@RestControllerAdvice` patterns. This document details all validation rules, exception handlers, and error response formats.

---

## 1. Validation Strategy

### 1.1 Bean Validation (JSR-380 / Hibernate Validator)

All incoming request DTOs are annotated with Bean Validation constraints. Controllers use `@Valid` to trigger validation.

### 1.2 Validation Annotations Used

| Annotation | Purpose | Used In |
|------------|---------|---------|
| `@NotBlank` | Rejects null and blank strings | All request DTOs |
| `@NotNull` | Rejects null values | CreateOrderRequest, etc. |
| `@Size(min, max)` | String length constraints | RegisterRequest, login |
| `@Email` | Email format validation | RegisterRequest, login |
| `@Min` / `@Max` | Numeric range | Quantity, price fields |
| `@Pattern` | Regex pattern matching | Order type, dietary type |
| `@NotEmpty` | Rejects null and empty collections | Order items, addresses |

---

## 2. Request DTO Validation Rules

### 2.1 AuthController — Register (`RegisterRequest`)

```
POST /api/v1/auth/register
```

| Field | Type | Constraints | Error Message |
|-------|------|-------------|---------------|
| `name` | String | `@NotBlank` | Name is required |
| `email` | String | `@NotBlank`, `@Email` (5-254 chars), `@Pattern` (email regex) | Valid email is required |
| `password` | String | `@NotBlank`, `@Size(min=8, max=64)` | Password must be 8-64 characters |
| `phone` | String | `@Pattern` (10-digit regex `^[6-9]\d{9}$`) | Invalid phone number |
| `role` | Enum | `@NotNull` | Role is required |

### 2.2 AuthController — Login (`AuthRequest`)

```
POST /api/v1/auth/login
```

| Field | Type | Constraints | Error Message |
|-------|------|-------------|---------------|
| `email` | String | `@NotBlank` | Email is required |
| `password` | String | `@NotBlank` | Password is required |

### 2.3 OrderController — Create Order (`CreateOrderRequest`)

```
POST /api/v1/orders
```

| Field | Type | Constraints | Error Message |
|-------|------|-------------|---------------|
| `idempotencyKey` | String | `@NotBlank` | Idempotency key is required |
| `cartId` | String | `@NotEmpty` | Cart ID is required |
| `deliveryAddress` | Object | `@NotNull` | Delivery address is required |
| `orderType` | String | `@Pattern(regexp="^(PICKUP\|DELIVERY)$")` | Order type must be PICKUP or DELIVERY |
| `paymentMethod` | String | `@Pattern(regexp="^(COD\|ONLINE)$")` | Invalid payment method |

### 2.4 MeController — Address (`AddressRequest`)

```
POST /api/v1/me/addresses
```

| Field | Type | Constraints | Error Message |
|-------|------|-------------|---------------|
| `label` | String | `@NotBlank` | Address label is required |
| `addressLine1` | String | `@NotBlank` | Address line 1 is required |
| `city` | String | `@NotBlank` | City is required |
| `state` | String | `@NotBlank` | State is required |
| `pincode` | String | `@NotBlank`, `@Size(min=6, max=6)`, `@Pattern` (6-digit regex) | Invalid pincode |
| `phone` | String | `@Pattern` (10-digit regex) | Invalid phone number |

### 2.5 MeController — Profile Update

```
PATCH /api/v1/me/profile
```

| Field | Type | Constraints | Error Message |
|-------|------|-------------|---------------|
| `name` | String | `@NotBlank` | Name is required |
| `phone` | String | `@Pattern` (optional but if present, must match) | Invalid phone number |

### 2.6 MenuController — Admin Create Category

```
POST /api/v1/admin/categories
```

| Field | Type | Constraints | Error Message |
|-------|------|-------------|---------------|
| `name` | String | `@NotBlank`, `@Size(max=100)` | Category name is required |
| `description` | String | `@Size(max=500)` | — |
| `displayOrder` | Integer | `@Min(0)` | Display order must be non-negative |

### 2.7 MenuController — Admin Create Item

```
POST /api/v1/admin/items
```

| Field | Type | Constraints | Error Message |
|-------|------|-------------|---------------|
| `categoryId` | Long | `@NotNull` | Category ID is required |
| `name` | String | `@NotBlank`, `@Size(max=255)` | Item name is required |
| `basePriceMinor` | Integer | `@NotNull`, `@Min(0)` | Price must be non-negative |
| `dietaryType` | Enum | `@Pattern` | Dietary type must be VEGETARIAN, NON_VEGETARIAN, or VEGAN |

### 2.8 MenuController — Admin Create ModifierGroup

```
POST /api/v1/admin/modifier-groups
```

| Field | Type | Constraints | Error Message |
|-------|------|-------------|---------------|
| `name` | String | `@NotBlank`, `@Size(max=100)` | Modifier group name is required |
| `minSelections` | Integer | `@Min(0)` | — |
| `maxSelections` | Integer | `@Min(1)` | Max selections must be at least 1 |
| `required` | Boolean | — | — |

### 2.9 DeliveryController — Availability Status

```
PATCH /api/v1/delivery/partner/status
```

| Field | Type | Constraints | Error Message |
|-------|------|-------------|---------------|
| `status` | String | `@Pattern` (ONLINE, OFFLINE, BUSY) | Invalid delivery status |

---

## 3. Global Exception Handling

### 3.1 GlobalExceptionHandler

File: `backend/src/main/java/com/restru/common/exception/GlobalExceptionHandler.java`

**Architecture:**
- `@RestControllerAdvice` — intercepts exceptions across all controllers
- 10 specific exception handler methods, each returning `ResponseEntity<ErrorResponse>`
- All errors return consistent `ErrorResponse` JSON structure

### 3.2 Error Response Format

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Validation failed for object='createOrderRequest'; errors: [idempotencyKey: Idempotency key is required]",
  "timestamp": "2025-01-15T10:30:00",
  "path": "/api/v1/orders"
}
```

### 3.3 Exception Handlers

| Exception Type | HTTP Status | Error Code | Trigger Conditions |
|---------------|-------------|------------|-------------------|
| `MethodArgumentNotValidException` | 400 | VALIDATION_ERROR | Invalid input fields |
| `ConstraintViolationException` | 400 | VALIDATION_ERROR | `@PathVariable`, `@RequestParam` constraint violation |
| `EntityNotFoundException` | 404 | NOT_FOUND | Entity not found by ID |
| `ResourceNotFoundException` | 404 | NOT_FOUND | Resource not found |
| `IllegalStateException` | 400 | BAD_REQUEST | Invalid state transition |
| `DataIntegrityViolationException` | 409 | CONFLICT | Duplicate key, FK violation |
| `AccessDeniedException` | 403 | FORBIDDEN | Insufficient role |
| `BadCredentialsException` | 401 | UNAUTHORIZED | Invalid email/password |
| `AuthenticationException` | 401 | UNAUTHORIZED | Missing/invalid token |
| `JwtException` | 401 | UNAUTHORIZED | Malformed/expired JWT |
| `Generic Exception` | 500 | INTERNAL_ERROR | Uncaught errors |

### 3.4 Handler Details

#### ValidationException Handler (400)
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex, HttpServletRequest request) {
    String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
    return buildErrorResponse(HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Validation failed: " + errors,
            request.getRequestURI());
}
```

#### EntityNotFound Handler (404)
```java
@ExceptionHandler(EntityNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(
        EntityNotFoundException ex, HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.NOT_FOUND,
            "NOT_FOUND",
            ex.getMessage(),
            request.getRequestURI());
}
```

#### IllegalState Handler (400)
```java
@ExceptionHandler(IllegalStateException.class)
public ResponseEntity<ErrorResponse> handleIllegalState(
        IllegalStateException ex, HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST,
            "BAD_REQUEST",
            ex.getMessage(),
            request.getRequestURI());
}
```

#### AccessDenied Handler (403)
```java
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ErrorResponse> handleAccessDenied(
        AccessDeniedException ex, HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.FORBIDDEN,
            "FORBIDDEN",
            "You do not have permission to access this resource",
            request.getRequestURI());
}
```

#### Authentication Handler (401)
```java
@ExceptionHandler({BadCredentialsException.class,
                 AuthenticationException.class,
                 JwtException.class})
public ResponseEntity<ErrorResponse> handleAuth(
        Exception ex, HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.UNAUTHORIZED,
            "UNAUTHORIZED",
            "Authentication failed: " + ex.getMessage(),
            request.getRequestURI());
}
```

---

## 4. Domain-Level Validation

### 4.1 Order State Transitions

```java
// OrderService.java — ALLOWED_TRANSITIONS
private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS =
    Map.ofEntries(
        Map.entry(PLACED, Set.of(ACCEPTED, REJECTED, CANCELLED)),
        Map.entry(ACCEPTED, Set.of(PREPARING, REJECTED, CANCELLED)),
        Map.entry(PREPARING, Set.of(READY_FOR_PICKUP)),
        Map.entry(READY_FOR_PICKUP, Set.of(PICKED_UP, DELIVERY_FAILED)),
        Map.entry(PICKED_UP, Set.of(OUT_FOR_DELIVERY)),
        Map.entry(OUT_FOR_DELIVERY, Set.of(DELIVERED, DELIVERY_FAILED))
    );
```

Invalid transitions throw `IllegalStateException("Invalid status transition: ...")`.

### 4.2 Cancellation Rules

Customer cancellation only allowed in `PLACED` or `ACCEPTED` status. Throws `IllegalStateException("Order cannot be cancelled in status: ...")` otherwise.

### 4.3 Delivery Assignment State Machine

```java
// DeliveryService.java
private static final Map<DeliveryStatus, Set<DeliveryStatus>> ALLOWED_TRANSITIONS =
    Map.ofEntries(
        Map.entry(UNASSIGNED, Set.of(OFFERED, ASSIGNED, CANCELLED)),
        Map.entry(OFFERED, Set.of(ACCEPTED, DECLINED, EXPIRED)),
        Map.entry(ASSIGNED, Set.of(ACCEPTED, DECLINED, EXPIRED)),
        Map.entry(ACCEPTED, Set.of(PICKED_UP, FAILED)),
        Map.entry(PICKED_UP, Set.of(COMPLETED, FAILED)),
        Map.entry(COMPLETED, Set.of())
    );
```

---

## 5. Security Validation

### 5.1 Password Requirements
- Minimum 8 characters (`@Size(min=8)` on `RegisterRequest.password`)
- BCrypt hashing with strength 10

### 5.2 JWT Validation
- Token must start with "Bearer "
- Signature must match secret key
- Expiration time enforced (24 hours)
- Claims structure validated (username, roles, issuedAt, expiration)

### 5.3 Role-Based Authorization
- `@PreAuthorize("hasRole('ADMIN')")` on admin endpoints
- `@PreAuthorize("hasRole('DELIVERY_GUY')")` on delivery endpoints
- `@PreAuthorize("isAuthenticated()")` on customer endpoints

### 5.4 Outlet Scoping
- Staff operations are scoped to their outlet:
```java
if (!order.getOutletId().equals(staff.getOutletId())) {
    throw new AccessDeniedException("Access denied to order from another outlet");
}
```

---

## 6. Frontend Validation

### 6.1 Client-Side Validation (TypeScript)

**Login/Register forms:**
- Email format validation
- Password minimum length
- Phone number format (10-digit, Indian format)

**Checkout flow:**
- Address form validation
- Required field checks

### 6.2 Server-Authoritative Pricing

Frontend displays pricing from `/api/v1/cart` response, but all prices are recomputed server-side:

```typescript
// CartService (backend) recomputes:
// - Subtotal from DB prices (not client-sent prices)
// - Tax at 5% rounded
// - Delivery fee (₹40 flat or free over ₹300 threshold)
```

Frontend cannot manipulate prices — the server validates cart item prices against the database.

---

## 7. Validation Flow

```
Client Request
     ↓
[ @Valid Annotation ]
     ↓
[ Bean Validation Constraints ]
     ├─ Pass → Controller Method
     └─ Fail → MethodArgumentNotValidException
                     ↓
           GlobalExceptionHandler
                     ↓
          [ 400 BAD_REQUEST + ErrorResponse ]
                     ↓
               Client receives
            structured error JSON
```

---

## 8. Error Code Reference

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Input field validation failed |
| `BAD_REQUEST` | 400 | Invalid business logic state (e.g., bad transition) |
| `UNAUTHORIZED` | 401 | Authentication missing or invalid |
| `FORBIDDEN` | 403 | User lacks required role/permission |
| `NOT_FOUND` | 404 | Entity or resource not found |
| `CONFLICT` | 409 | Duplicate resource, constraint violation |
| `INTERNAL_ERROR` | 500 | Unhandled server error |

---

## 9. Test Coverage of Validation

### AuthControllerTest — Validation Tests
- `registerWithInvalidEmailFails()` — invalid email format
- `registerWithShortPasswordFails()` — password too short
- `registerDuplicateEmailFails()` — duplicate email (409)

### OrderControllerTest — Validation Tests
- `createOrderWithoutIdempotencyKeyFails()` — missing idempotency key
- `invalidStatusTransitionRejected()` — bad state transition

### CartControllerTest — Tests
- `cartRecomputesPricingFromDatabase()` — server-side pricing authority

### DeliveryControllerTest — Validation Tests
- `setPartnerAvailability()` — delivery status validation
- `reportDeliveryIssue()` — issue type validation