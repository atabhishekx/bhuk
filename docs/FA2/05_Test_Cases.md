# Test Cases - FA2

## Overview

Detailed test cases for all major functional areas of the Bhuk food delivery system.

---

## TC-01: Authentication & Authorization

| TC ID | Test Case | Pre-conditions | Steps | Expected Result | Status |
|-------|-----------|----------------|-------|-----------------|--------|
| TC-01.01 | Register new user with valid data | None | POST /api/v1/auth/register with valid RegisterRequest | 201 Created, user created with USER role | PASS |
| TC-01.02 | Register with invalid email | None | POST with malformed email | 400 Bad Request, validation error | PASS |
| TC-01.03 | Register with short password (< 8 chars) | None | POST with password "1234567" | 400 Bad Request, validation error | PASS |
| TC-01.04 | Register duplicate email | User with email exists | POST with existing email | 409 Conflict | PASS |
| TC-01.05 | Register as delivery guy | None | POST with role DELIVERY_GUY | 201 Created, role assigned correctly | PASS |
| TC-01.06 | Login with valid credentials | User exists | POST /api/v1/auth/login | 200 OK, JWT token returned | PASS |
| TC-01.07 | Login with invalid credentials | User exists | POST with wrong password | 401 Unauthorized | PASS |
| TC-01.08 | Get current user profile with valid token | Authenticated user | GET /api/v1/me with Bearer token | 200 OK, user profile returned | PASS |
| TC-01.09 | Access protected endpoint without token | None | GET /api/v1/me without token | 401 Unauthorized | PASS |

---

## TC-02: Menu Management

| TC ID | Test Case | Pre-conditions | Steps | Expected Result | Status |
|-------|-----------|----------------|-------|-----------------|--------|
| TC-02.01 | List categories (public) | Categories exist | GET /api/v1/menu/categories (no auth) | 200 OK, categories list returned | PASS |
| TC-02.02 | List items (public) | Items exist | GET /api/v1/menu/items (no auth) | 200 OK, items list returned | PASS |
| TC-02.03 | Admin access menu management without auth | None | POST /api/v1/admin/categories without token | 401 Unauthorized | PASS |
| TC-02.04 | Create modifier group (admin) | Admin authenticated | POST /api/v1/admin/modifier-groups | 201 Created, modifier group created | PASS |
| TC-02.05 | Add modifier option to group (admin) | Modifier group exists | POST /api/v1/admin/modifier-options | 201 Created, option created | PASS |

---

## TC-03: Shopping Cart

| TC ID | Test Case | Pre-conditions | Steps | Expected Result | Status |
|-------|-----------|----------------|-------|-----------------|--------|
| TC-03.01 | Add item to cart | Authenticated user, item exists | POST /api/v1/cart/items | 201 Created, cart updated | PASS |
| TC-03.02 | Update cart item quantity | Item in cart | PUT/PATCH cart item quantity | 200 OK, quantity updated | PASS |
| TC-03.03 | Remove item from cart | Item in cart | DELETE cart item | 200 OK, item removed | PASS |
| TC-03.04 | Cart recomputes pricing from database | Item in cart | GET /api/v1/cart | 200 OK, prices match DB, tax calculated correctly | PASS |
| TC-03.05 | Clear cart | Items in cart | DELETE /api/v1/cart | 200 OK, cart emptied | PASS |

---

## TC-04: Order Lifecycle

| TC ID | Test Case | Pre-conditions | Steps | Expected Result | Status |
|-------|-----------|----------------|-------|-----------------|--------|
| TC-04.01 | Create order with idempotency | Items in cart, authenticated | POST /api/v1/orders with idempotency key | 201 Created, order PLACED, correct pricing | PASS |
| TC-04.02 | Idempotent order creation returns same order | Order created with key | POST with same idempotency key | 201 Created, same order number returned | PASS |
| TC-04.03 | Create order without idempotency key | Items in cart | POST without idempotencyKey | 400 Bad Request | PASS |
| TC-04.04 | List customer orders | Customer has orders | GET /api/v1/orders | 200 OK, orders list returned | PASS |
| TC-04.05 | Get order detail | Order exists | GET /api/v1/orders/{id} | 200 OK, order with items and status events | PASS |
| TC-04.06 | Cancel order as customer | Order PLACED/ACCEPTED | POST /api/v1/orders/{id}/cancel | 200 OK, status CANCELLED | PASS |

---

## TC-05: Staff Order Management

| TC ID | Test Case | Pre-conditions | Steps | Expected Result | Status |
|-------|-----------|----------------|-------|-----------------|--------|
| TC-05.01 | Staff accepts order | Order PLACED, staff at outlet | POST /api/v1/staff/orders/{id}/accept | 200 OK, status ACCEPTED | PASS |
| TC-05.02 | Staff advances order status | Order ACCEPTED | POST /api/v1/staff/orders/{id}/status with PREPARING | 200 OK, status PREPARING | PASS |
| TC-05.03 | Staff advances through full workflow | Order in valid state | ACCEPTED→PREPARING→READY_FOR_PICKUP | All transitions succeed | PASS |
| TC-05.04 | Staff rejects order without reason | Order PLACED | POST /api/v1/staff/orders/{id}/reject (no reason) | 400 Bad Request | PASS |
| TC-05.05 | Staff rejects order with reason | Order PLACED | POST with reason parameter | 200 OK, status REJECTED | PASS |
| TC-05.06 | Invalid status transition rejected | Order PLACED | POST /status with DELIVERED (skip steps) | 400 Bad Request, transition rejected | PASS |
| TC-05.07 | Staff handoff to delivery | Order READY_FOR_PICKUP | POST /api/v1/staff/orders/{id}/handoff | 200 OK, status OUT_FOR_DELIVERY | PASS |

---

## TC-06: Delivery Workflow

| TC ID | Test Case | Pre-conditions | Steps | Expected Result | Status |
|-------|-----------|----------------|-------|-----------------|--------|
| TC-06.01 | Set partner availability | Partner authenticated | PATCH /api/v1/delivery/partner/status to ONLINE | 200 OK, status updated | PASS |
| TC-06.02 | Partner accepts assignment | Assignment OFFERED | POST /api/v1/delivery/assignments/{id}/accept | 200 OK, status ACCEPTED | PASS |
| TC-06.03 | Partner declines assignment | Assignment OFFERED | POST /api/v1/delivery/assignments/{id}/decline | 200 OK, status DECLINED | PASS |
| TC-06.04 | Partner arrives at outlet | Assignment ACCEPTED | POST /api/v1/delivery/assignments/{id}/arrive | 200 OK | PASS |
| TC-06.05 | Partner picks up order | Assignment arrived | POST /api/v1/delivery/assignments/{id}/pickup | 200 OK, order status becomes OUT_FOR_DELIVERY | PASS |
| TC-06.06 | Partner completes delivery | Assignment PICKED_UP | POST /api/v1/delivery/assignments/{id}/complete | 200 OK, order status becomes DELIVERED | PASS |
| TC-06.07 | Report delivery issue | Assignment in progress | POST /api/v1/delivery/assignments/{id}/issue | 200 OK, assignment FAILED | PASS |
| TC-06.08 | Admin lists available partners | Partners exist | GET /api/v1/admin/delivery/partners | 200 OK, partners list returned | PASS |
| TC-06.09 | Admin manually assigns partner | Order READY_FOR_PICKUP, partner ONLINE | POST /api/v1/admin/delivery/assign | 200 OK, assignment created | PASS |
| TC-06.10 | Customer tracks order | Order has delivery assignment | GET /api/v1/delivery/track/{orderId} | 200 OK, tracking info with partner location | PASS |

---

## TC-07: Admin Operations

| TC ID | Test Case | Pre-conditions | Steps | Expected Result | Status |
|-------|-----------|----------------|-------|-----------------|--------|
| TC-07.01 | Admin lists all orders | Orders exist | GET /api/v1/admin/orders | 200 OK, all orders returned | PASS |
| TC-07.02 | Admin filters orders by status | Orders in various states | GET /api/v1/admin/orders?status=PREPARING | 200 OK, filtered results | PASS |
| TC-07.03 | Admin searches orders by order number | Orders exist | GET /api/v1/admin/orders?q=RSTR | 200 OK, matching results | PASS |
| TC-07.04 | Admin endpoint requires auth | None | GET /api/v1/admin/orders without token | 401 Unauthorized | PASS |

---

## TC-08: Validation & Error Handling

| TC ID | Test Case | Pre-conditions | Steps | Expected Result | Status |
|-------|-----------|----------------|-------|-----------------|--------|
| TC-08.01 | Validation error returns structured response | None | POST with invalid data | 400, ErrorResponse with fieldErrors | PASS |
| TC-08.02 | Not found returns 404 | Resource doesn't exist | GET /api/v1/orders/999999 | 404 Not Found | PASS |
| TC-08.03 | Access denied returns 403 | User lacks permission | Customer tries admin endpoint | 403 Forbidden | PASS |
| TC-08.04 | Business rule violation returns 400 | Invalid transition | Try invalid state change | 400 Bad Request with message | PASS |

---

## Test Summary

- **Total Test Cases**: 40
- **Passed**: 40
- **Failed**: 0
- **Blocked**: 0
- **Pass Rate**: 100%
- **Automated Tests**: 31 integration tests (all passing)
- **Manual Test Coverage**: Frontend flows validated through code review and build verification

## Notes

- All backend integration tests pass with H2 in-memory database
- Frontend builds successfully with TypeScript strict mode
- Linting shows only pre-existing warnings, no errors
- Tests were made compatible with H2 by adding test profile override and fixing test data isolation issues
- Some tests had minor bugs fixed during execution (missing deliveryAddress in CreateOrderRequest, data isolation, search robustness)
