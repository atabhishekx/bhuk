# Requirements Specification

## Actors

| Actor | Description | Authentication |
|-------|-------------|----------------|
| **Customer** | End user who browses menu, places orders, tracks delivery | JWT (USER role) |
| **Restaurant Staff (OUTLET_STAFF)** | Kitchen/order manager at specific outlet | JWT (OUTLET_STAFF role, outlet-scoped) |
| **Delivery Partner** | Person who picks up and delivers orders | JWT (DELIVERY_GUY role) |
| **Administrator** | System owner with full access across outlets | JWT (ADMIN role) |
| **Payment Gateway (Razorpay)** | External payment processor | Webhook signature verification |

## Functional Requirements

### FR-01: User Authentication
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-01.1 | Customer can register with email, password, name, phone | High |
| FR-01.2 | Customer can login with email/password → returns JWT | High |
| FR-01.3 | JWT contains: userId, email, roles, expiry (24h) | High |
| FR-01.4 | Password hashed with BCrypt (cost 10) | High |
| FR-01.5 | Admin can assign roles to users (ADMIN, OUTLET_STAFF, DELIVERY_GUY) | High |
| FR-01.6 | Token refresh via `/api/v1/me` endpoint | Medium |
| FR-01.7 | Logout clears client-side token | Medium |

### FR-02: Menu Browsing (Public)
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-02.1 | List all active categories for an outlet | High |
| FR-02.2 | List all active items (filter by category, outlet) | High |
| FR-02.3 | Get item details: name, description, price, image, dietary type, modifiers | High |
| FR-02.4 | Show item availability (outlet-specific) | High |
| FR-02.5 | Check serviceability by pincode | Medium |

### FR-03: Shopping Cart (Customer)
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-03.1 | Authenticated user has one active cart per outlet | High |
| FR-03.2 | Add item to cart with quantity and selected modifiers | High |
| FR-03.3 | Update item quantity (min 1, remove if 0) | High |
| FR-03.4 | Remove item from cart | High |
| FR-03.5 | Clear entire cart | High |
| FR-03.6 | View cart with authoritative pricing (server-calculated) | High |
| FR-03.7 | Checkout quote: subtotal, tax (5%), delivery fee, grand total | High |
| FR-03.8 | Validate item availability and outlet match before add | High |

### FR-04: Order Creation (Customer)
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-04.1 | Create order from cart with idempotency key | High |
| FR-04.2 | Capture delivery address (structured + JSON snapshot) | High |
| FR-04.3 | Server recalculates all prices from DB (no client trust) | High |
| FR-04.4 | Create order items with price snapshots | High |
| FR-04.5 | Clear cart after successful order | High |
| FR-04.6 | Return order with order_number, status PLACED | High |
| FR-04.7 | Duplicate idempotency key → return existing order | High |

### FR-05: Order Management (Customer)
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-05.1 | List own orders (paginated, newest first) | High |
| FR-05.2 | View order detail with items, status history, tracking | High |
| FR-05.3 | Cancel order if status allows (PLACED, CONFIRMED) | High |
| FR-05.4 | View payment status for order | Medium |

### FR-06: Order Processing (Restaurant Staff)
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-06.1 | View outlet-scoped order queue (filter by status) | High |
| FR-06.2 | Accept order (PLACED → CONFIRMED) | High |
| FR-06.3 | Reject order with reason (PLACED → CANCELLED) | High |
| FR-06.4 | Advance status: CONFIRMED → PREPARING → READY_FOR_PICKUP | High |
| FR-06.5 | Handoff to delivery (READY_FOR_PICKUP → OUT_FOR_DELIVERY) | High |
| FR-06.6 | Auto-create delivery offer on READY_FOR_PICKUP | High |

### FR-07: Delivery Operations (Delivery Partner)
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-07.1 | View available orders for pickup (near outlet) | High |
| FR-07.2 | Accept order assignment (OUT_FOR_DELIVERY) | High |
| FR-07.3 | Mark pickup (OUT_FOR_DELIVERY → picked up) | High |
| FR-07.4 | Mark delivered (DELIVERED) | High |
| FR-07.5 | Update GPS location during delivery | Medium |
| FR-07.6 | Set availability (online/offline) | Medium |
| FR-07.7 | Report delivery issue (customer unavailable, address wrong, etc.) | Medium |

### FR-08: Order Tracking (Customer)
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-08.1 | Real-time order status view | High |
| FR-08.2 | Delivery partner location (when OUT_FOR_DELIVERY) | Medium |
| FR-08.3 | Estimated delivery time | Medium |
| FR-08.4 | Status history timeline | High |

### FR-09: Admin Management
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-09.1 | CRUD: Categories (name, description, display_order, active) | High |
| FR-09.2 | CRUD: Items (category, name, description, price, image, dietary, active) | High |
| FR-09.3 | CRUD: Modifier groups (name, min/max select, required) | High |
| FR-09.4 | CRUD: Modifier options (group, name, price_delta) | High |
| FR-09.5 | Link/unlink items to modifier groups | High |
| FR-09.6 | Set outlet-specific availability & price override | High |
| FR-09.7 | User management: list, assign roles, view details | High |
| FR-09.8 | Outlet management: CRUD, hours, status | High |
| FR-09.9 | Operations report: orders, revenue, avg time by outlet | Medium |

### FR-10: Payment (Deferred - Webhook Only)
| ID | Requirement | Priority |
|----|-------------|----------|
| FR-10.1 | Create payment intent (Razorpay) | Low |
| FR-10.2 | Webhook handler for payment.captured/failed | Low |
| FR-10.3 | Reconcile payment status with order | Low |
| FR-10.4 | Refund initiation (admin) | Low |

## Non-Functional Requirements

| ID | Requirement | Verification |
|----|-------------|--------------|
| NFR-01 | API response < 500ms (p95) for GET endpoints | Load test |
| NFR-02 | JWT validation < 50ms | Unit test |
| NFR-03 | BCrypt cost factor 10 | Code review |
| NFR-04 | All mutating endpoints require authentication | Integration test |
| NFR-05 | Role-based access enforced at controller level | Integration test |
| NFR-06 | Database FK constraints prevent orphans | Schema review |
| NFR-07 | Optimistic locking on Order entity | Code review |
| NFR-08 | Idempotency keys unique per user | DB constraint |
| NFR-09 | No sensitive data in logs (passwords, tokens) | Log review |
| NFR-10 | CORS configured for frontend origin only | Config review |
| NFR-11 | Input validation on all DTOs (@Valid) | Code review |
| NFR-12 | Global exception handler returns consistent format | Integration test |
| NFR-13 | Database migrations idempotent and versioned | Flyway validate |
| NFR-14 | Frontend builds without TypeScript errors | `npm run build` |
| NFR-15 | No console errors in browser during normal flow | Manual test |

## Data Requirements

### Menu Data (Seeded)
- 7 categories: North Indian, Rice & Biryani, South Indian, Snacks, Sides, Desserts, Beverages
- 50 items with images, dietary type (veg/non_veg), base prices
- 3 modifier groups: Spice Level, Add-ons, Ice Option
- 8 modifier options with price deltas

### User Data (Seeded)
- 1 Admin: admin@restru.local / admin123
- 1 Delivery: delivery@restru.local / admin123
- 1 Customer: customer@restru.local / admin123
- 1 Outlet: Restru Main Branch, Bengaluru

## Interface Requirements

### API Contracts
- **Format**: JSON
- **Wrapper**: `ApiResponse<T>` with success, data, message, requestId
- **Errors**: `ErrorResponse` with code, message, details, requestId
- **Dates**: ISO 8601 UTC
- **Money**: Integer minor units (paise) in API, formatted ₹X.XX in UI

### Frontend Routes
| Route | Access | Description |
|-------|--------|-------------|
| `/` | Public | Redirect to /menu or /login |
| `/login` | Public | Login page |
| `/register` | Public | Registration page |
| `/menu` | Auth (USER) | Menu browsing |
| `/cart` | Auth (USER) | Cart management |
| `/checkout` | Auth (USER) | Address + place order |
| `/orders` | Auth (USER) | Order history |
| `/orders/:id` | Auth (USER) | Order detail + tracking |
| `/profile` | Auth (USER) | Profile, addresses |
| `/admin/*` | ADMIN | Admin dashboard |
| `/delivery/*` | DELIVERY_GUY | Delivery dashboard |
| `/staff/*` | OUTLET_STAFF | Staff dashboard |

## Error Scenarios

| Scenario | HTTP Status | Error Code |
|----------|-------------|------------|
| Invalid credentials | 401 | AUTH_INVALID_CREDENTIALS |
| Token expired/invalid | 401 | AUTH_TOKEN_EXPIRED |
| Access denied (role) | 403 | AUTH_INSUFFICIENT_ROLE |
| Resource not found | 404 | RESOURCE_NOT_FOUND |
| Validation failed | 400 | VALIDATION_ERROR |
| Duplicate idempotency key | 409 | IDEMPOTENCY_CONFLICT |
| Invalid state transition | 409 | INVALID_STATE_TRANSITION |
| Item unavailable | 409 | ITEM_UNAVAILABLE |
| Database constraint | 500 | INTERNAL_ERROR |

## Acceptance Criteria for FA1 (Menu → Cart → Order)

1. Customer can login and browse menu with categories
2. Customer can add items to cart with modifiers
3. Cart shows correct server-calculated totals
4. Customer can proceed to checkout, enter address
5. Order created successfully with order_number
6. Order appears in customer's order history
7. Order status starts at PLACED
8. All prices calculated server-side from DB
9. No duplicate orders on retry (idempotency)
10. Database reflects order, items, status event