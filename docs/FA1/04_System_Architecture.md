# System Architecture

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            BHUK SYSTEM ARCHITECTURE                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐    HTTPS/REST/JSON     ┌──────────────┐                  │
│  │   FRONTEND   │ ◄─────────────────────► │   BACKEND    │                  │
│  │  (React 19)  │    Port 5173 (dev)      │ (Spring Boot)│                  │
│  │  Port 5173   │                         │  Port 8080   │                  │
│  └──────────────┘                         └──────┬───────┘                  │
│         │                                        │                           │
│         │                                        │                           │
│  ┌──────▼──────┐                         ┌────────▼────────┐               │
│  │  BROWSER    │                         │   MYSQL DB      │               │
│  │  LOCALSTORE │                         │  (restru_db)    │               │
│  │  (JWT Token)│                         │  Port 3306      │               │
│  └─────────────┘                         └─────────────────┘               │
│                                                                             │
│  ┌──────────────┐                         ┌─────────────────┐               │
│  │  RAZORPAY    │                         │  FILE STORAGE   │               │
│  │  (External)  │                         │  (Local/Cloud)  │               │
│  │  Webhooks    │                         │  Menu Images    │               │
│  └──────────────┘                         └─────────────────┘               │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

### Frontend (React + TypeScript + Vite)
| Component | Responsibility |
|-----------|----------------|
| **App.tsx** | Route definitions, AuthProvider wrapper, public/private routes |
| **AuthContext** | Global auth state (user, token), login/register/logout, token persistence |
| **axios.ts** | Axios instance with request/response interceptors, JWT attachment |
| **Pages** | Feature-specific UI: Menu, Cart, Checkout, Orders, Admin, Delivery, Staff |
| **Components** | Reusable UI: Navbar, CartItem, PageLayout, Toast, Common (Button, Input, Modal) |

### Backend (Spring Boot 3.2.5)
| Layer | Components | Responsibility |
|-------|------------|----------------|
| **Controller** | AuthController, MenuController, CartController, OrderController, DeliveryController, AdminUserController, HealthController | HTTP endpoints, request validation, response formatting |
| **Service** | AuthService, UserService, MenuService, CartService, OrderService, DeliveryService, OrderNumberGenerator | Business logic, transactions, orchestration |
| **Repository** | Spring Data JPA interfaces (extends JpaRepository) | Data access, custom queries |
| **Entity** | JPA entities (@Entity, @Table) | DB mapping, relationships, constraints |
| **DTO** | Request/Response classes | API contracts, validation, serialization |
| **Security** | SecurityConfig, JwtAuthenticationFilter, JwtUtil, CustomUserDetailsService | JWT auth, RBAC, filter chain |
| **Exception** | GlobalExceptionHandler, custom exceptions | Centralized error handling |

### Database (MySQL)
- **Engine**: InnoDB
- **Charset**: utf8mb4_unicode_ci
- **Migrations**: Flyway (9 versions)
- **Naming**: snake_case tables/columns, singular table names

## Data Flow: Customer Order Workflow

```
1. BROWSE MENU
   Frontend: GET /api/v1/menu/categories + GET /api/v1/menu/items
   Backend:  MenuService.listCategories() → MenuCategoryRepository
             MenuService.listItems() → MenuItemRepository
   DB:       SELECT * FROM menu_categories, menu_items

2. ADD TO CART
   Frontend: POST /api/v1/cart/items {itemId, quantity, modifiers}
   Backend:  CartService.addToCart() → CartRepository, CartItemRepository
   DB:       INSERT/UPDATE carts, cart_items, cart_item_modifiers

3. CHECKOUT QUOTE
   Frontend: GET /api/v1/cart/checkout/quote
   Backend:  CartService.quote() → Recalculates from DB
   DB:       JOIN carts, cart_items, menu_items, modifier_options

4. PLACE ORDER
   Frontend: POST /api/v1/orders {idempotencyKey, deliveryAddress}
   Backend:  OrderService.createOrder()
             - Validate idempotency
             - Recalculate prices from DB
             - Create Order, OrderItems, OrderItemModifiers
             - Create OrderStatusEvent (PLACED)
             - Clear cart
   DB:       Transaction: INSERT orders, order_items, order_item_modifiers,
                       order_status_events, idempotency_records
             DELETE cart_items

5. ORDER PROCESSING (Staff)
   Frontend: POST /api/v1/staff/orders/{id}/status {newStatus}
   Backend:  OrderService.staffAdvanceStatus()
             - Validate transition
             - Update Order.status
             - Create OrderStatusEvent
             - If READY_FOR_PICKUP → DeliveryService.createOfferForOrder()
   DB:       UPDATE orders, INSERT order_status_events, delivery_assignments

6. DELIVERY (Partner)
   Frontend: POST /api/v1/delivery/assign, /pickup, /delivered, /location
   Backend:  DeliveryService methods
   DB:       UPDATE delivery_assignments, delivery_tracking
```

## Security Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      SECURITY LAYERS                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. TRANSPORT: HTTPS (production) / HTTP (dev)                 │
│                                                                 │
│  2. CORS: WebConfig → allowedOrigins: http://localhost:5173    │
│                                                                 │
│  3. AUTHENTICATION:                                             │
│     • JWT in Authorization: Bearer <token> header              │
│     • JwtAuthenticationFilter (Before UsernamePasswordAuthFilter)│
│     • Validates: signature, expiry, user exists                │
│     • Sets SecurityContext with UserDetails                    │
│                                                                 │
│  4. AUTHORIZATION:                                              │
│     • SecurityConfig: permitAll for /auth/**, /menu/**, /health │
│     • @PreAuthorize("hasRole('ADMIN')") on admin endpoints     │
│     • @PreAuthorize on staff/delivery endpoints                │
│     • Method-level security via @EnableMethodSecurity          │
│                                                                 │
│  5. PASSWORD STORAGE:                                           │
│     • BCryptPasswordEncoder (cost 10)                          │
│     • Never logged or returned in API                          │
│                                                                 │
│  6. INPUT VALIDATION:                                           │
│     • @Valid on all request DTOs                               │
│     • Bean Validation annotations (@NotNull, @Size, @Email)    │
│     • GlobalExceptionHandler maps to 400 responses             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## API Design Principles

| Principle | Implementation |
|-----------|----------------|
| **RESTful** | Resource-based URLs, HTTP verbs, status codes |
| **Versioned** | `/api/v1/` prefix |
| **Consistent Responses** | `ApiResponse<T>` wrapper for all endpoints |
| **Idempotent Writes** | `Idempotency-Key` header on POST /orders |
| **Pagination** | Pageable on list endpoints (future) |
| **Filtering** | Query parameters (status, outletId, q) |
| **Error Format** | RFC 7807-inspired ErrorResponse |

## Deployment Architecture (Production)

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   NGINX     │────►│  SPRING BOOT │────►│    MYSQL    │
│  (Reverse   │     │   (JAR)      │     │  (Primary)  │
│   Proxy)    │     │  Port 8080   │     │  Port 3306  │
└─────────────┘     └─────────────┘     └─────────────┘
       │                   │                    │
       ▼                   ▼                    ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  STATIC     │     │  HEALTH     │     │  BACKUP/    │
│  FILES      │     │  CHECKS     │     │  REPLICA    │
│  (Frontend) │     │  /health    │     │             │
└─────────────┘     └─────────────┘     └─────────────┘
```

## Technology Justification

| Choice | Reason |
|--------|--------|
| **Spring Boot 3** | Mature, enterprise-grade, excellent ecosystem, Java 17 LTS |
| **Spring Data JPA** | Reduces boilerplate, type-safe queries, transaction management |
| **Spring Security** | Industry standard, JWT integration, method-level security |
| **Flyway** | Version-controlled SQL migrations, repeatable deployments |
| **MySQL** | ACID compliant, widely supported, free, performs well |
| **React 19** | Component-based, large ecosystem, TypeScript support |
| **Vite** | Fast dev server, optimized production builds |
| **Tailwind CSS** | Utility-first, no context switching, small bundle |
| **React Router v6** | Modern hooks-based routing, nested routes |
| **Axios** | Interceptors, TypeScript support, widely used |

## Integration Points

| Integration | Protocol | Status |
|-------------|----------|--------|
| **Frontend ↔ Backend** | REST/JSON | ✅ Implemented |
| **Backend ↔ MySQL** | JDBC | ✅ Implemented |
| **Backend ↔ Razorpay** | HTTPS/Webhooks | 🔄 Webhook only |
| **Frontend ↔ Browser Storage** | localStorage | ✅ Implemented |
| **Menu Images** | Static file serving | ✅ /public/menu-images/ |

## Scalability Considerations

1. **Stateless Services** - JWT auth, no server sessions → horizontal scaling ready
2. **Connection Pooling** - HikariCP (default 10 connections)
3. **Database Indexes** - On foreign keys, status columns, search fields
4. **Read Replicas** - Can add for menu/order read-heavy workloads
5. **Caching** - Redis can be added for menu, categories (future)
6. **Async Processing** - Order events can go to message queue (future)

## Monitoring & Observability

| Aspect | Implementation |
|--------|----------------|
| **Logging** | SLF4J + Logback, structured, requestId correlation |
| **Health Check** | `/api/v1/health` (Spring Boot Actuator style) |
| **Error Tracking** | GlobalExceptionHandler with requestId |
| **SQL Logging** | `hibernate.format_sql: true` (dev only) |
| **Audit Trail** | `order_status_events` table for all order transitions |