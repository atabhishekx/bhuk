# OOP Implementation Analysis

## Overview

The Bhuk food delivery system demonstrates strong Object-Oriented Programming (OOP) principles across its backend architecture. This document analyzes how each OOP principle is implemented.

---

## 1. Abstraction

### 1.1 Interface-Based Design

The codebase uses Java interfaces to define contracts for data access:

```
OrderRepository          →  JpaRepository<Order, Long>
OrderStatusEventRepository → JpaRepository<OrderStatusEvent, Long>
IdempotencyRecordRepository → JpaRepository<IdempotencyRecord, Long>
UserRepository           → JpaRepository<User, Long>
MenuItemRepository       → JpaRepository<MenuItem, Long>
MenuCategoryRepository   → JpaRepository<MenuCategory, Long>
CartRepository           → JpaRepository<Cart, Long>
CartItemRepository       → JpaRepository<CartItem, Long>
ModifierGroupRepository  → JpaRepository<ModifierGroup, Long>
ModifierOptionRepository → JpaRepository<ModifierOption, Long>
DeliveryAssignmentRepository → JpaRepository<DeliveryAssignment, Long>
DeliveryPartnerRepository → JpaRepository<DeliveryPartner, Long>
```

### 1.2 Abstract Entities

All entities extend a common `BaseEntity` (inferred from Lombok `@Data` annotations and JPA patterns) with shared fields:
- `id` (Long)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

### 1.3 Service Layer Abstraction

Each service exposes only the methods needed by controllers:
- `OrderService` — order CRUD, state transitions, idempotency
- `CartService` — cart management, authoritative pricing
- `MenuService` — menu browsing, admin CRUD
- `DeliveryService` — delivery workflow, assignment logic
- `AuthService` — registration, authentication

### 1.4 DTO Pattern for API Contracts

DTOs abstract the database representation from the API contract:
- `CreateOrderRequest` — input for order creation
- `OrderResponse` — rich response with computed fields
- `CartResponse` — cart with pricing breakdown
- `OperationsReportResponse` — aggregated admin report

---

## 2. Encapsulation

### 2.1 Private Fields with Accessors

All entity fields are private with getter/setter accessors (via Lombok `@Getter`/`@Setter`):

```java
// Order.java
private Long id;
private Long userId;
private Long outletId;
private Integer totalAmountMinor;        // private, accessed via getter
private Integer taxAmountMinor;
private Integer deliveryFeeMinor;
private OrderStatus status;
private LocalDateTime placedAt;
private LocalDateTime deliveredAt;
private String deliveryAddressJson;     // encapsulated as JSON string
private String cancellationReason;
```

### 2.2 Behavior Methods on Entities

Business logic is encapsulated within entity classes:

**Order.java — transition validation:**
```java
public boolean canTransitionTo(OrderStatus newStatus) {
    return ALLOWED_TRANSITIONS.getOrDefault(status, Set.of())
            .contains(newStatus);
}
```

**Order.java — status change:**
```java
public void changeStatus(OrderStatus newStatus, String reason) {
    if (!canTransitionTo(newStatus)) {
        throw new IllegalStateException(
            "Cannot transition from " + status + " to " + newStatus);
    }
    this.status = newStatus;
    // ... timestamp handling
}
```

### 2.3 Validation Encapsulated in DTOs

```java
public class CreateOrderRequest {
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotNull(message = "Delivery address is required")
    private AddressRequest deliveryAddress;

    @NotEmpty(message = "Cart ID is required")
    private String cartId;

    @Pattern(regexp = "^(PICKUP|DELIVERY)$")
    private String orderType = "DELIVERY";
}
```

---

## 3. Inheritance

### 3.1 JPA Entity Inheritance (BaseEntity)

While not using explicit `@Inheritance`, the entities share a common pattern through composition:

```
Order                  → has OrderItems, OrderStatusEvents
DeliveryAssignment     → has LocationUpdates (list)
User                   → has UserAddress entries
MenuItem               → has ModifierGroups
ModifierGroup          → has ModifierOptions
Cart                   → has CartItems
CartItem               → has CartItemModifiers
```

### 3.2 Role Hierarchy Concept

Roles are modeled as an enum with implicit hierarchy:
```java
public enum UserRole {
    USER,        // Customer role
    DELIVERY_GUY, // Delivery partner role
    ADMIN        // Super-user role (can do everything)
}
```

The hierarchy is enforced in `SecurityConfig` role checks, not through inheritance.

---

## 4. Polymorphism

### 4.1 Exception Hierarchy

The global exception handler demonstrates polymorphism by handling different exception types:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) { ... }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) { ... }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) { ... }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) { ... }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) { ... }

    // ... 5 more handlers
}
```

Each handler provides type-specific error response construction.

### 4.2 State-Dependent Behavior

The `Order` entity's `canTransitionTo()` method uses the current `status` (which is a polymorphic `enum` type) to determine allowed next states:

```java
public enum OrderStatus {
    PLACED, ACCEPTED, PREPARING, READY_FOR_PICKUP,
    PICKED_UP, OUT_FOR_DELIVERY, DELIVERED,
    REJECTED, CANCELLED, DELIVERY_FAILED
}
```

### 4.3 Enum Polymorphism for Dietary Types

```java
// MenuItem.java
public enum DietaryType {
    VEGETARIAN,    // Green indicator on frontend
    NON_VEGETARIAN, // Red indicator
    VEGAN          // Plant-based icon
}
```

Different values produce different frontend rendering and filtering behavior.

---

## 5. Composition over Inheritance

### 5.1 Order Composition

```java
// Order.java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<OrderItem> items = new ArrayList<>();

@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<OrderStatusEvent> statusEvents = new ArrayList<>();

@OneToOne(cascade = CascadeType.ALL)
@JoinColumn(name = "delivery_assignment_id")
private DeliveryAssignment deliveryAssignment;
```

### 5.2 Cart Composition

```java
// Cart.java
@OneToMany(mappedBy = "cart", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
private List<CartItem> items = new ArrayList<>();
```

### 5.3 MenuItem Composition

```java
// MenuItem.java
@OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL)
private List<ModifierGroup> modifierGroups;
```

---

## 6. SOLID Principles Application

### 6.1 Single Responsibility Principle (SRP)

| Class | Responsibility |
|-------|----------------|
| `OrderService` | Order business logic (creation, transitions, cancellation) |
| `OrderNumberGenerator` | Order number sequence generation |
| `CartService` | Cart operations and server-side pricing |
| `GlobalExceptionHandler` | Centralized error handling |
| `JwtUtil` | JWT token creation and validation |
| `OrderController` | Order-related HTTP endpoints |

### 6.2 Open/Closed Principle (OCP)

The `GlobalExceptionHandler` is open for extension (new `@ExceptionHandler` methods) without modifying existing handlers.

The `ALLOWED_TRANSITIONS` map in `OrderService` allows adding new transitions without changing transition logic.

### 6.3 Liskov Substitution Principle (LSP)

Spring Security's `UserDetailsService` interface is implemented by `CustomUserDetailsService`, which is substitutable with any implementation.

### 6.4 Interface Segregation Principle (ISP)

Repositories are fine-grained:
- `OrderRepository` — order-specific queries
- `OrderStatusEventRepository` — event-specific queries
- `IdempotencyRecordRepository` — idempotency-specific queries

No fat interface forces clients to depend on methods they don't use.

### 6.5 Dependency Inversion Principle (DIP)

Services depend on repository interfaces, not concrete implementations:

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final OrderStatusEventRepository orderStatusEventRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
}
```

All dependencies are injected as abstractions (interfaces via Spring Data JPA).

---

## 7. Design Patterns Identified

### 7.1 Repository Pattern
- `OrderRepository`, `UserRepository`, etc. — abstract data access

### 7.2 Service Layer Pattern
- `OrderService`, `CartService`, `DeliveryService` — business logic orchestration

### 7.3 Data Transfer Object (DTO) Pattern
- `OrderResponse`, `CreateOrderRequest`, `CartResponse` — API contract objects

### 7.4 Singleton Pattern
- Spring-managed beans (all `@Service`, `@Controller`, `@Component` are singletons by default)

### 7.5 Builder Pattern (via Lombok)
- `@Builder` annotation on DTOs like `OrderResponse`

### 7.6 Strategy Pattern (Pricing)
- `PricingResult` record acts as a pricing strategy object:

```java
public record PricingResult(
    Integer subtotalMinor,
    Integer taxAmountMinor,
    Integer deliveryFeeMinor,
    Integer totalAmountMinor,
    Map<String, Object> breakdown
) { }
```

### 7.7 State Machine Pattern (Order Status)
- `ALLOWED_TRANSITIONS` map enforces valid state transitions
- `Order.canTransitionTo()` method encapsulates transition logic

### 7.8 Factory Pattern (Record Creation)
- `OrderStatusEvent.from(Order, status, reason)` factory method:

```java
public static OrderStatusEvent from(Order order, OrderStatus status, String reason) {
    OrderStatusEvent event = new OrderStatusEvent();
    event.setOrder(order);
    event.setStatus(status);
    event.setReason(reason);
    event.setTimestamp(LocalDateTime.now());
    return event;
}
```

### 7.9 Audit Trail / Event Sourcing (partial)
- `OrderStatusEvent` entities record every status change with timestamp and reason

---

## 8. Class Diagram Summary

```
┌─────────────────────────────────────────────────────┐
│                  <<interface>>                      │
│          Spring Data JPA Repository                 │
└─────────────────────────────────────────────────────┘
                      ▲
    ┌───────────────┼───────────────┐
    │               │               │
┌───┴────┐    ┌────┴────┐    ┌─────┴────┐
│OrderRepository │  UserRepository │  MenuCategoryRepository │
└────────┘    └─────────┘    └──────────┘

┌─────────────────┐    ┌──────────────────┐
│    Order        │    │ OrderStatusEvent │
├─────────────────┤    ├──────────────────┤
│- id             │    │- id             │
│- userId         │    │- order (FK)      │
│- outletId       │    │- status         │
│- totalAmountMinor│   │- reason          │
│- status          │    │- timestamp       │
│- deliveryAddress │   │                  │
│+ canTransitionTo()│  │                  │
│+ changeStatus()   │   │                  │
│+ addStatusEvent() │   │                  │
└─────────────────┘    └──────────────────┘
    │ 1                          ▲ *
    │                                    │
    │ * (items)                       │ from Order
    ▼                                    │
┌──────────────┐
│ OrderItem    │
├──────────────┤
│- id           │
│- orderId      │
│- itemId       │
│- itemName     │ (snapshot)
│- unitPriceMinor│(snapshot)
│- quantity     │
│- totalPriceMinor│
└──────────────┘
```

---

## 9. OOP Quality Assessment

| Metric | Assessment |
|--------|-----------|
| **Encapsulation** | Excellent — all fields private, behavior methods on entities |
| **Abstraction** | Excellent — DTOs separate persistence from API contracts |
| **Inheritance** | Good — composition preferred over inheritance |
| **Polymorphism** | Good — exception handling, enum-based behavior |
| **SOLID Compliance** | High — SRP clearly followed, DIP via repository injection |
| **Design Pattern Usage** | Strong — Repository, Service, DTO, State Machine, Factory, Strategy |
| **Code Reuse** | Good — `BaseEntity` pattern, factory methods, record-based strategies |