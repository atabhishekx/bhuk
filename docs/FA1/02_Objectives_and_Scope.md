# Objectives and Scope

## Project Objectives

### Primary Objectives
1. **Build a complete food delivery platform** for a single restaurant chain
2. **Enable end-to-end customer workflow**: Menu → Cart → Checkout → Order → Tracking
3. **Provide restaurant operations interface**: Order queue, status management, kitchen display
4. **Support delivery fleet management**: Partner assignment, tracking, issue reporting
5. **Implement admin dashboard**: Menu CRUD, user management, outlet management, reports

### Technical Objectives
1. **RESTful API** with Spring Boot 3.x and Java 17
2. **Modern React frontend** with TypeScript, Vite, Tailwind CSS
3. **Secure authentication** with JWT, role-based access control
4. **Relational database** with MySQL, versioned migrations (Flyway)
5. **Clean architecture** - layered (controller/service/repository), DTO pattern
5. **Observability** - structured logging, request tracing, error handling

## Scope

### In-Scope Features (MVP)

| Module | Features |
|--------|----------|
| **Authentication** | JWT login/register, role assignment, token validation, password hashing |
| **Menu Management** | Categories, items, modifier groups/options, outlet-specific availability, images, dietary tags |
| **Shopping Cart** | Persistent cart per user, modifiers, quantity management, server-side price calculation |
| **Order Management** | Idempotent creation, state machine (PLACED→DELIVERED), status audit trail, cancellation |
| **Delivery Operations** | Partner availability, order assignment, pickup/delivery confirmation, location updates |
| **Admin Dashboard** | Menu CRUD, order search/filter, user/role management, outlet management |
| **Customer Profile** | Address book, order history, profile management |

### Non-Functional Requirements

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-01 | **Response Time** | 95th percentile < 500ms for read APIs |
| NFR-02 | **Availability** | 99.9% uptime (excluding maintenance) |
| NFR-03 | **Security** | OWASP Top 10 compliance, BCrypt passwords, JWT with 24h expiry |
| NFR-04 | **Data Integrity** | ACID transactions, FK constraints, optimistic locking |
| NFR-05 | **Scalability** | Horizontal scaling ready (stateless services) |
| NFR-06 | **Audit Trail** | All order status changes logged with actor, timestamp, reason |
| NFR-07 | **Idempotency** | Order creation safe to retry (idempotency key) |
| NFR-08 | **Price Integrity** | All prices calculated server-side from DB |

## Assumptions

1. **Single chain** - One brand, multiple outlets, shared menu with outlet overrides
2. **Self-hosted** - Runs on restaurant's infrastructure (or cloud VM)
3. **MySQL database** - XAMPP/MySQL 8.x for development
4. **JWT stateless auth** - No server-side session store
5. **Indian market** - INR currency (paise storage), pincode-based serviceability
6. **No payment gateway** in MVP - Razorpay config present but integration deferred
7. **No real-time** - Polling-based tracking (WebSocket deferred)
8. **Single language** - English UI (i18n deferred)
9. **No multi-tenancy** - Single database, single application instance

## Exclusions (Out of Scope for FA1/FA2)

| Feature | Reason |
|---------|--------|
| Multi-restaurant marketplace | Single-chain focus |
| Payment gateway integration | Razorpay webhook only, no client SDK |
| WebSocket/real-time updates | Complexity, polling sufficient for MVP |
| Email/SMS notifications | External service dependency |
| Loyalty program / coupons | Business feature, not core platform |
| Multi-language (i18n) | Scope creep |
| Advanced analytics/BI | Can use DB directly |
| Mobile apps (iOS/Android) | PWA/web sufficient for MVP |
| Kubernetes/Docker deployment | Documented but not implemented |
| Load testing / performance benchmarks | Not required for academic assessment |

## Business Rules

### Order State Transitions
```
PLACED → CONFIRMED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED
                ↘ CANCELLED (before OUT_FOR_DELIVERY)
```
- Only forward transitions allowed (except cancellation)
- Each transition logs: actor, old/new status, timestamp, reason

### Pricing Rules
- All prices stored as **integer paise** (1 INR = 100 paise)
- Tax: 5% on subtotal (configurable)
- Delivery fee: ₹30 base + ₹5/km beyond 3km
- Modifiers: price delta added to base price
- **Server recalculates** all totals - never trust client

### Role Permissions
| Role | Menu | Cart | Orders | Delivery | Admin |
|------|------|------|--------|----------|-------|
| CUSTOMER | Read | CRUD | Own (create/view/cancel) | Track own | - |
| OUTLET_STAFF | Read | - | Outlet-scoped (accept/reject/advance) | Handoff | - |
| DELIVERY_GUY | - | - | Assigned (pickup/deliver/location) | Self-manage | - |
| ADMIN | Full CRUD | - | All outlets | Full | Full |

### Idempotency
- Client generates `idempotencyKey` (e.g., `order-${timestamp}-${random}`)
- Server stores in `idempotency_records` with request hash
- Duplicate key + same hash → return cached response
- Expires after 24 hours

## Constraints

1. **Academic timeline** - FA1 (analysis + 1 module), FA2 (75-80% complete)
2. **No external paid services** - All components self-contained
3. **Team size** - Single developer (simulated)
4. **Technology lock-in** - Spring Boot, React, MySQL fixed