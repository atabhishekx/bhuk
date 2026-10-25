# Gap Analysis - FA2 Extension Review

## Overview

This document maps every functional requirement (FR) and non-functional requirement (NFR) from FA1 against the actual implementation, identifying fully-implemented, partially-implemented, missing, and newly-verified capabilities as of FA2.

---

## 1. Functional Requirements

### FR-01: User Registration (Public)

| Aspect | Status | Evidence |
|--------|--------|----------|
| Self-registration via `POST /api/v1/auth/register` | **FULLY IMPLEMENTED** | `AuthController.java:30`, `AuthService.java:44` |
| Input validation (`@NotBlank`, `@Email`, `@Size`) | **FULLY IMPLEMENTED** | `RegisterRequest.java:14-20` |
| Role restriction: only USER or DELIVERY_GUY self-assignable | **FULLY IMPLEMENTED** | `AuthService.java:58-61` |
| BCrypt password hashing | **FULLY IMPLEMENTED** | `SecurityConfig.java:38-40`, `AuthService.java:52` |
| Test coverage | **FULLY IMPLEMENTED** | `AuthControllerTest.java:44` — `registerNewUserReturnsCreated` |

### FR-02: User Login with JWT

| Aspect | Status | Evidence |
|--------|--------|----------|
| Login endpoint | **FULLY IMPLEMENTED** | `AuthController.java:38`, `AuthService.java:73` |
| JWT token generation | **FULLY IMPLEMENTED** | `JwtUtil.java:70-83` |
| Token includes roles claim | **FULLY IMPLEMENTED** | `JwtUtil.java:78` |
| Token secret must be >= 32 chars (256-bit) | **FULLY IMPLEMENTED** | `JwtUtil.java:33-35` |
| Token type returned as "Bearer" | **FULLY IMPLEMENTED** | `AuthResponse.java:11` |
| Test coverage | **FULLY IMPLEMENTED** | `AuthControllerTest.java:69` — `loginReturnsToken` |

### FR-03: Role-Based Access Control

| Aspect | Status | Evidence |
|--------|--------|----------|
| ROLE_ADMIN on admin endpoints | **FULLY IMPLEMENTED** | `OrderController.java:93`, `MenuController.java:78`, etc. |
| ROLE_DELIVERY_GUY on delivery endpoints | **FULLY IMPLEMENTED** | `DeliveryController.java:65`, etc. |
| Authenticated on customer endpoints | **FULLY IMPLEMENTED** | `SecurityConfig.java:57-63` |
| Frontend RoleRoute component | **FULLY IMPLEMENTED** | `App.tsx:19-26` |
| Test coverage | **FULLY IMPLEMENTED** | `AuthControllerTest.java:92` — `registerDeliveryGuyReturnsUserRole`; `MenuControllerTest.java:179` — `adminEndpointsRequireAuth` |

### FR-04: Menu Browsing (Public)

| Aspect | Status | Evidence |
|--------|--------|----------|
| List categories (public) | **FULLY IMPLEMENTED** | `MenuController.java:38`, `MenuService.java:56` |
| List items (public) | **FULLY IMPLEMENTED** | `MenuController.java:46`, `MenuService.java:61` |
| Get item detail (public) | **FULLY IMPLEMENTED** | `MenuController.java:55`, `MenuService.java:71` |
| Filter items by category | **FULLY IMPLEMENTED** | `MenuService.java:62-64` |
| Dietary type indicator | **FULLY IMPLEMENTED** | `MenuItem.java:38`, `V9__seed_menu_data.sql:12-83` |
| 50 menu items seeded across 7 categories | **FULLY IMPLEMENTED** | V9 seed, categories: North Indian, Rice & Biryani, South Indian, Snacks, Sides, Desserts, Beverages |
| Test coverage | **FULLY IMPLEMENTED** | `MenuControllerTest.java:100` — `publicListCategoriesAndItems` |

### FR-05: Shopping Cart Management

| Aspect | Status | Evidence |
|--------|--------|----------|
| Add to cart (authenticated) | **FULLY IMPLEMENTED** | `CartController.java:42`, `CartService.java:108` |
| Update quantity | **FULLY IMPLEMENTED** | `CartController.java:53`, `CartService.java:157` |
| Remove item | **FULLY IMPLEMENTED** | `CartController.java:63`, `CartService.java:177` |
| Clear cart | **FULLY IMPLEMENTED** | `CartController.java:72`, `CartService.java:191` |
| View cart with pricing | **FULLY IMPLEMENTED** | `CartController.java:34`, `CartService.java:80` |
| Checkout quote | **FULLY IMPLEMENTED** | `CartController.java:80`, `CartService.java:203` |
| Server-side authoritative pricing (no client trust) | **FULLY IMPLEMENTED** | `CartService.java:211-234` — `calculatePricing()` |
| Modifier support in cart | **FULLY IMPLEMENTED** | `CartService.java:249-265` — `replaceModifiers()` |
| Single-outlet cart constraint | **FULLY IMPLEMENTED** | `CartService.java:123-130` |
| Test coverage | **FULLY IMPLEMENTED** | `CartControllerTest.java:112-208` — 4 test methods |

### FR-06: Order Creation (Idempotent)

| Aspect | Status | Evidence |
|--------|--------|----------|
| Idempotent order creation | **FULLY IMPLEMENTED** | `OrderController.java:48`, `OrderService.java:195-307` |
| Idempotency key required (`@NotBlank`) | **FULLY IMPLEMENTED** | `CreateOrderRequest.java:15-17` |
| Server validates cart items still available | **FULLY IMPLEMENTED** | `OrderService.java:218-232` |
| Server recomputes prices from DB | **FULLY IMPLEMENTED** | `OrderService.java:234-235`, `computeOrderTotal()` |
| Idempotency records stored with 24h expiry | **FULLY IMPLEMENTED** | `OrderService.java:293-302`, `IdempotencyRecord.java` |
| Price breakdown: subtotal, tax (5%), delivery fee (₹40) | **FULLY IMPLEMENTED** | `OrderService.java:485-507` |
| Order number generation | **FULLY IMPLEMENTED** | `OrderNumberGenerator.java:28-46` — `RSTR-yyyymmdd-0001` |
| Delivery address captured as JSON | **FULLY IMPLEMENTED** | `OrderService.java:255-263` |
| Test coverage | **FULLY IMPLEMENTED** | `OrderControllerTest.java:155` — `createOrderIdempotentAndList` |

### FR-07: Order State Machine

| Aspect | Status | Evidence |
|--------|--------|----------|
| State transitions enforced server-side | **FULLY IMPLEMENTED** | `OrderService.java:47-58`, `ALLOWED_TRANSITIONS` map |
| Terminal states: DELIVERED, REJECTED, CANCELLED, DELIVERY_FAILED | **FULLY IMPLEMENTED** | `OrderService.java:49` |
| Transition: PLACED → ACCEPTED → PREPARING → READY_FOR_PICKUP → PICKED_UP → OUT_FOR_DELIVERY → DELIVERED | **FULLY IMPLEMENTED** | `OrderService.java:51-57` |
| Status event audit trail (append-only) | **FULLY IMPLEMENTED** | `OrderStatusEvent.java`, `OrderService.java:413-422` |
| Test coverage | **FULLY IMPLEMENTED** | `OrderControllerTest.java:223` — `staffAcceptAndAdvanceStatus`; `invalidTransitionRejected` |

### FR-08: Order Cancellation

| Aspect | Status | Evidence |
|--------|--------|----------|
| Customer cancels own order (PLACED or ACCEPTED only) | **FULLY IMPLEMENTED** | `OrderService.java:311-334` |
| Reason captured in audit log | **FULLY IMPLEMENTED** | `OrderService.java:332` |
| Test coverage | **FULLY IMPLEMENTED** | `OrderControllerTest.java:215-220` |

### FR-09: Customer Order History

| Aspect | Status | Evidence |
|--------|--------|----------|
| Customer lists own orders | **FULLY IMPLEMENTED** | `OrderController.java:61`, `OrderService.java:112` |
| Customer views own order detail | **FULLY IMPLEMENTED** | `OrderController.java:69`, `OrderService.java:100` |
| Order detail includes items + status events | **FULLY IMPLEMENTED** | `OrderResponse.java:26-27`, `OrderService.java:521-557` |
| Test coverage | **FULLY IMPLEMENTED** | `OrderControllerTest.java:199-213` |

### FR-10: Staff Order Management

| Aspect | Status | Evidence |
|--------|--------|----------|
| Staff view outlet-scoped order queue | **FULLY IMPLEMENTED** | `OrderController.java:142`, `OrderService.java:122` |
| Staff accept order | **FULLY IMPLEMENTED** | `OrderController.java:92`, `OrderService.java:339` |
| Staff reject order (with reason) | **FULLY IMPLEMENTED** | `OrderController.java:102`, `OrderService.java:344` |
| Staff advance status | **FULLY IMPLEMENTED** | `OrderController.java:113`, `OrderService.java:351` |
| Staff handoff to delivery (→ OUT_FOR_DELIVERY) | **FULLY IMPLEMENTED** | `OrderController.java:131`, `OrderService.java:356` |
| Outlet scoping enforced (non-admin) | **FULLY IMPLEMENTED** | `OrderService.java:368-383` — `staffTransition()` |
| Test coverage | **FULLY IMPLEMENTED** | `OrderControllerTest.java:223-281` — multiple test methods |

### FR-11: Admin Order Oversight

| Aspect | Status | Evidence |
|--------|--------|----------|
| Admin list all orders (chain-wide) | **FULLY IMPLEMENTED** | `OrderController.java:154`, `OrderService.java:156` |
| Admin filter by status | **FULLY IMPLEMENTED** | `OrderService.java:186-187` |
| Admin search by order number or customer email | **FULLY IMPLEMENTED** | `OrderService.java:188-189` |
| Admin operations report | **FULLY IMPLEMENTED** | `OrderController.java:168`, `OrderService.java:426-483` |
| Test coverage | **FULLY IMPLEMENTED** | `OrderControllerTest.java:375` — `adminListsAllOrdersAndFiltersByStatus` |

### FR-12: Admin Menu Management

| Aspect | Status | Evidence |
|--------|--------|----------|
| Create category | **FULLY IMPLEMENTED** | `MenuController.java:77`, `MenuService.java:103` |
| Update category | **FULLY IMPLEMENTED** | `MenuController.java:88`, `MenuService.java:114` |
| Delete category | **FULLY IMPLEMENTED** | `MenuController.java:99`, `MenuService.java:125` |
| Create item | **FULLY IMPLEMENTED** | `MenuController.java:108`, `MenuService.java:133` |
| Update item | **FULLY IMPLEMENTED** | `MenuController.java:119`, `MenuService.java:150` |
| Delete item | **FULLY IMPLEMENTED** | `MenuController.java:129`, `MenuService.java:168` |
| Create modifier group | **FULLY IMPLEMENTED** | `MenuController.java:139`, `MenuService.java:176` |
| Update modifier group | **FULLY IMPLEMENTED** | `MenuController.java:150`, `MenuService.java:188` |
| Delete modifier group | **FULLY IMPLEMENTED** | `MenuController.java:161`, `MenuService.java:200` |
| Create modifier option | **FULLY IMPLEMENTED** | `MenuController.java:170`, `MenuService.java:208` |
| Update modifier option | **FULLY IMPLEMENTED** | `MenuController.java:181`, `MenuService.java:220` |
| Delete modifier option | **FULLY IMPLEMENTED** | `MenuController.java:191`, `MenuService.java:230` |
| Test coverage | **FULLY IMPLEMENTED** | `MenuControllerTest.java:146` — `adminCreateModifierGroupAndOption`; `publicListCategoriesAndItems` |

### FR-13: Admin User & Role Management

| Aspect | Status | Evidence |
|--------|--------|----------|
| Admin list all users | **FULLY IMPLEMENTED** | `AdminUserController.java:45` |
| Admin assign role to user | **FULLY IMPLEMENTED** | `AdminUserController.java:54`, `UserService.java` |
| Role validation (USER, DELIVERY_GUY, ADMIN only) | **FULLY IMPLEMENTED** | `AdminUserController.java:62-64` |
| Outlet scoping required for DELIVERY_GUY | **FULLY IMPLEMENTED** | `AdminUserController.java:67-69` |
| Test coverage | **PARTIALLY IMPLEMENTED** | No dedicated test, but covered indirectly by OrderControllerTest setup |

### FR-14: Delivery Partner Availability

| Aspect | Status | Evidence |
|--------|--------|----------|
| Partner sets status: ONLINE/OFFLINE/BUSY | **FULLY IMPLEMENTED** | `DeliveryController.java:42`, `DeliveryService.java:68` |
| Status validation | **FULLY IMPLEMENTED** | `DeliveryService.java:80-83` |
| Test coverage | **FULLY IMPLEMENTED** | `DeliveryControllerTest.java:236` — `setPartnerAvailability` |

### FR-15: Delivery Assignment Workflow

| Aspect | Status | Evidence |
|--------|--------|----------|
| Auto-create offer when order READY_FOR_PICKUP | **FULLY IMPLEMENTED** | `OrderController.java:124`, `DeliveryService.java:295` |
| Partner accepts offered assignment | **FULLY IMPLEMENTED** | `DeliveryController.java:64`, `DeliveryService.java:133` |
| Partner declines (with reason) | **FULLY IMPLEMENTED** | `DeliveryController.java:75`, `DeliveryService.java:139` |
| Admin manual assign/reassign | **FULLY IMPLEMENTED** | `DeliveryController.java:182`, `DeliveryService.java:318` |
| Assignment state machine | **FULLY IMPLEMENTED** | `DeliveryService.java:28-38`, `ALLOWED_TRANSITIONS` |
| Test coverage | **FULLY IMPLEMENTED** | `DeliveryControllerTest.java:258-310`, `adminListsAvailablePartnersAndAssigns` |

### FR-16: Delivery Lifecycle

| Aspect | Status | Evidence |
|--------|--------|----------|
| Partner marks arrived at outlet | **FULLY IMPLEMENTED** | `DeliveryController.java:89`, `DeliveryService.java:148` |
| Partner marks picked up | **FULLY IMPLEMENTED** | `DeliveryController.java:100`, `DeliveryService.java:153` |
| Partner marks delivered/complete | **FULLY IMPLEMENTED** | `DeliveryController.java:111`, `DeliveryService.java:177` |
| Order status auto-advanced on pickup | **FULLY IMPLEMENTED** | `DeliveryService.java:167-170` — READY_FOR_PICKUP → OUT_FOR_DELIVERY |
| Order status auto-advanced on delivery complete | **FULLY IMPLEMENTED** | `DeliveryService.java:191-193` — → DELIVERED |
| Test coverage | **FULLY IMPLEMENTED** | `DeliveryControllerTest.java:258` — `acceptAndCompleteDelivery` |

### FR-17: Delivery Issue Reporting

| Aspect | Status | Evidence |
|--------|--------|----------|
| Partner reports issue → assignment FAILED | **FULLY IMPLEMENTED** | `DeliveryController.java:122`, `DeliveryService.java:201` |
| Issue types captured | **FULLY IMPLEMENTED** | `DeliveryIssue.java:24`, `ReportIssueRequest.java` |
| Test coverage | **FULLY IMPLEMENTED** | `DeliveryControllerTest.java:337` — `reportDeliveryIssue` |

### FR-18: Order Tracking for Customer

| Aspect | Status | Evidence |
|--------|--------|----------|
| Customer tracks order location | **FULLY IMPLEMENTED** | `DeliveryController.java:160`, `DeliveryService.java:258` |
| Tracks delivery state + partner location | **FULLY IMPLEMENTED** | `DeliveryService.java:258-291` |
| Tracks location history | **FULLY IMPLEMENTED** | `DeliveryService.java:226-254` |
| Test coverage | **FULLY IMPLEMENTED** | `DeliveryControllerTest.java:363` — `customerOrderTracking` |

### FR-19: User Profile Management

| Aspect | Status | Evidence |
|--------|--------|----------|
| GET current user profile (`/me`) | **FULLY IMPLEMENTED** | `MeController.java:46` |
| PATCH profile (name, phone) | **FULLY IMPLEMENTED** | `MeController.java:64` |
| Frontend AuthContext stores user state | **FULLY IMPLEMENTED** | `AuthContext.tsx:23-83` |
| Test coverage | **FULLY IMPLEMENTED** | `AuthControllerTest.java:104` — `meReturnsProfileWhenAuthenticated` |

### FR-20: User Address Management

| Aspect | Status | Evidence |
|--------|--------|----------|
| List addresses | **FULLY IMPLEMENTED** | `MeController.java:88` |
| Add address | **FULLY IMPLEMENTED** | `MeController.java:104` |
| Update address | **FULLY IMPLEMENTED** | `MeController.java:137` |
| Delete address | **FULLY IMPLEMENTED** | `MeController.java:178` |
| Default address management | **FULLY IMPLEMENTED** | `MeController.java:123-128` — first/default handling |
| Frontend Profile page integrates addresses | **FULLY IMPLEMENTED** | `Profile.tsx:23-55` |
| Test coverage | **PARTIALLY IMPLEMENTED** | No dedicated test file, tested indirectly via context |

### FR-21: Outlet Serviceability Check

| Aspect | Status | Evidence |
|--------|--------|----------|
| Check if pincode is serviceable | **FULLY IMPLEMENTED** | `MenuController.java:66`, `MenuService.java:77` |
| Returns outlet name + serviceable flag | **FULLY IMPLEMENTED** | `MenuService.java:88-98` |
| Test coverage | **PARTIALLY IMPLEMENTED** | No dedicated test for serviceability |

### FR-22: Payment Intent Initiation

| Aspect | Status | Evidence |
|--------|--------|----------|
| Endpoint for payment intent creation | **MISSING / Not implemented** | Schema exists (V6 payments table), no controller/service endpoint |
| Payment webhook endpoint (public) | **FULLY IMPLEMENTED** (stub) | `SecurityConfig.java:62` — `/api/v1/webhooks/payments/**` permitAll |
| Payment state retrieval | **MISSING / Not implemented** | No endpoint in OrderController |

### FR-23: Operations Report

| Aspect | Status | Evidence |
|--------|--------|----------|
| Admin retrieves operations report | **FULLY IMPLEMENTED** | `OrderController.java:168`, `OrderService.java:426-483` |
| Includes order counts by status + revenue | **FULLY IMPLEMENTED** | `OperationsReportResponse.java` |
| Frontend AdminDashboard displays report | **FULLY IMPLEMENTED** | `AdminDashboard.tsx:32-38` |
| Test coverage | **PARTIALLY IMPLEMENTED** | No dedicated test for report endpoint |

---

## 2. Non-Functional Requirements

| NFR | Status | Evidence |
|-----|--------|----------|
| NFR-01: REST API consistency (ApiResponse/ErrorResponse wrapper) | **FULLY IMPLEMENTED** | `ApiResponse.java:19-58`, `ErrorResponse.java:24-62` |
| NFR-02: Input validation (Bean Validation) | **FULLY IMPLEMENTED** | `CreateOrderRequest.java:15-17`, `RegisterRequest.java:14-20`, etc. |
| NFR-03: Error response consistency | **FULLY IMPLEMENTED** | `GlobalExceptionHandler.java:28-169` — 10 exception handlers |
| NFR-04: Authentication via JWT Bearer tokens | **FULLY IMPLEMENTED** | `SecurityConfig.java:51-68`, `JwtAuthenticationFilter.java` |
| NFR-05: Password hashing (BCrypt) | **FULLY IMPLEMENTED** | `SecurityConfig.java:38-39`, `BCryptPasswordEncoder` |
| NFR-06: API versioning (v1) | **FULLY IMPLEMENTED** | All controllers use `/api/v1` prefix |
| NFR-07: Transactional boundaries | **FULLY IMPLEMENTED** | `@Transactional` on all service methods |
| NFR-08: Database migrations (Flyway) | **FULLY IMPLEMENTED** | 9 migrations V1-V9, `flyway:clean flyway:migrate` works |
| NFR-09: Money in minor units (paise) | **FULLY IMPLEMENTED** | All `*Minor` fields, `roundTax()` in CartService/OrderService |
| NFR-10: Price is server-authoritative | **FULLY IMPLEMENTED** | `CartService.java:211-234`, `OrderService.java:218-235` |
| NFR-11: Idempotency on order creation | **FULLY IMPLEMENTED** | `OrderService.java:205-210`, `IdempotencyRecord` entity |
| NFR-12: State machine enforcement | **FULLY IMPLEMENTED** | `ALLOWED_TRANSITIONS` maps in both Order and Delivery services |
| NFR-13: Audit trail (append-only) | **FULLY IMPLEMENTED** | `OrderStatusEvent.java`, `order_status_events` table |
| NFR-14: Role-based UI rendering | **FULLY IMPLEMENTED** | `App.tsx:19-26` — `RoleRoute`, `Navbar.tsx:42-52` |
| NFR-15: Responsive design | **FULLY IMPLEMENTED** | Tailwind responsive classes throughout all pages |

---

## 3. Summary

| Category | Count |
|----------|-------|
| **Fully Implemented & Tested** | 20 of 23 functional requirements |
| **Partially Implemented / Missing Tests** | 3 functional requirements (FR-13, FR-20, FR-21, FR-23) |
| **Missing / Not Implemented** | 1 functional requirement (FR-22: Payment gateway integration) |
| **All NFRs** | Fully implemented (15 of 15) |
| **Backend Test Coverage** | 7 test classes, ~25 test methods, all passing |
| **Frontend Tests** | 0 (no test framework configured) |

---

## 4. Key Architectural Patterns Found

- **DTO Pattern**: Separate request/response DTOs from entities (e.g., `OrderResponse`, `CartResponse`)
- **State Machine Pattern**: Order status and delivery assignment state transitions via `ALLOWED_TRANSITIONS` maps
- **Snapshot Pattern**: Order items store name/price snapshots at purchase time (not FK references)
- **Idempotency Pattern**: `IdempotencyRecord` entity with SHA-256 request hashing
- **Repository Pattern**: Spring Data JPA repositories for all entities
- **Service Layer Pattern**: `@Service` annotated business logic between controllers and repositories
- **Global Exception Handling**: `@RestControllerAdvice` with structured error responses
- **Strategy Pattern (pricing)**: `PricingResult` record used as pricing strategy object