# API Specification

**Status:** Draft contract — paths and schemas must be finalized in OpenAPI before implementation.

## 1. Conventions

- Base path: `/api/v1` (proposed).
- JSON request/response bodies unless documented otherwise.
- Authentication required for customer-specific, staff, partner, and admin routes.
- Use server-side authorization and outlet scoping.
- Return consistent errors:
  ```json
  {
    "error": {
      "code": "ITEM_UNAVAILABLE",
      "message": "One or more items are no longer available.",
      "requestId": "request-id"
    }
  }
  ```
- Use UTC timestamps in ISO 8601 format.
- Use integer minor units for money, with currency explicitly identified.
- Checkout/order creation must support an idempotency key.
- Exact field schemas and status enums must be documented in `openapi.yaml` before coding.

## 2. Customer and menu endpoints

| Method | Path | Purpose | Access |
|---|---|---|---|
| GET | `/menu/categories` | List active categories | Public or authenticated |
| GET | `/menu/items` | List/search/filter available items | Public or authenticated |
| GET | `/menu/items/{itemId}` | Item details and modifiers | Public or authenticated |
| GET | `/outlets/serviceability` | Check address/serviceability | Public or authenticated; minimize address data |
| GET | `/outlets` | List eligible outlets if customer selection is supported | Public or authenticated |
| GET | `/me` | Current profile | Customer |
| PATCH | `/me` | Update own profile | Customer |
| GET | `/me/addresses` | List own addresses | Customer |
| POST | `/me/addresses` | Add address | Customer |
| PATCH | `/me/addresses/{addressId}` | Update own address | Customer |
| DELETE | `/me/addresses/{addressId}` | Delete own address, subject to retention rules | Customer |

## 3. Cart and checkout endpoints

| Method | Path | Purpose | Access |
|---|---|---|---|
| GET | `/cart` | Retrieve current cart | Customer |
| POST | `/cart/items` | Add an item and modifiers | Customer |
| PATCH | `/cart/items/{cartItemId}` | Change quantity/modifiers | Customer |
| DELETE | `/cart/items/{cartItemId}` | Remove an item | Customer |
| POST | `/checkout/quote` | Validate cart and calculate current quote | Customer |
| POST | `/orders` | Create order using idempotency key | Customer |
| GET | `/orders` | List own orders | Customer |
| GET | `/orders/{orderId}` | View own order and status | Customer |
| POST | `/orders/{orderId}/cancel` | Request cancellation if eligible | Customer |

A quote is advisory until order creation revalidates all inputs. Client totals are never authoritative.

## 4. Restaurant operations endpoints

| Method | Path | Purpose | Access |
|---|---|---|---|
| GET | `/staff/orders` | Outlet-scoped order queue | Staff/manager |
| POST | `/staff/orders/{orderId}/accept` | Accept order | Staff/manager for outlet |
| POST | `/staff/orders/{orderId}/reject` | Reject with reason | Staff/manager for outlet |
| POST | `/staff/orders/{orderId}/status` | Advance preparation state | Staff/manager for outlet |
| POST | `/staff/orders/{orderId}/handoff` | Record delivery handoff | Authorized staff |

## 5. Delivery partner endpoints

| Method | Path | Purpose | Access |
|---|---|---|---|
| PATCH | `/partner/availability` | Set online/offline availability | Delivery partner |
| GET | `/partner/assignments` | List offers and active assignments | Delivery partner |
| POST | `/partner/assignments/{assignmentId}/accept` | Accept assignment | Offered partner |
| POST | `/partner/assignments/{assignmentId}/decline` | Decline with reason | Offered partner |
| POST | `/partner/deliveries/{deliveryId}/arrived` | Mark arrival at outlet | Assigned partner |
| POST | `/partner/deliveries/{deliveryId}/pickup` | Confirm pickup | Assigned partner |
| POST | `/partner/deliveries/{deliveryId}/complete` | Mark delivery complete | Assigned partner |
| POST | `/partner/deliveries/{deliveryId}/issue` | Report failed delivery/issue | Assigned partner |

## 6. Admin endpoints

| Method | Path | Purpose | Access |
|---|---|---|---|
| CRUD | `/admin/menu/categories` | Manage categories | Admin |
| CRUD | `/admin/menu/items` | Manage menu items | Admin |
| CRUD | `/admin/menu/modifier-groups` | Manage modifiers | Admin |
| CRUD | `/admin/outlets` | Manage outlets | Admin |
| CRUD | `/admin/delivery-zones` | Manage delivery coverage/fees | Admin |
| GET | `/admin/orders` | Search/filter orders (query params: `status`, `outletId`, `q` for order number/customer email) | Authorized admin/support |
| POST | `/admin/deliveries/{deliveryId}/assign` | Manually assign/reassign partner | Admin/manager with scope |
| GET | `/admin/reports/operations` | Basic operational metrics | Authorized manager/admin |

CRUD means the specific GET/POST/PATCH/DELETE operations should be individually defined and permissioned.

`GET /admin/orders` returns orders sorted by createdAt desc. `ADMIN` sees all outlets; `STAFF` is scoped to their assigned outlet and cannot list another outlet's orders.

## 7. Suggested status codes

- `200 OK`: successful read/update.
- `201 Created`: resource created.
- `202 Accepted`: asynchronous work accepted but not complete.
- `400 Bad Request`: malformed request.
- `401 Unauthorized`: missing/invalid authentication.
- `403 Forbidden`: authenticated but not permitted.
- `404 Not Found`: missing resource or intentionally concealed unauthorized resource.
- `409 Conflict`: invalid state transition, idempotency conflict, or resource conflict.
- `422 Unprocessable Entity`: validation/business-rule failure.
- `429 Too Many Requests`: rate limit.
- `500 Internal Server Error`: unexpected failure without leaking internals.
- `502/503`: provider/dependency failure where appropriate.

## 8. API implementation requirements

- Publish a valid OpenAPI schema with request/response types, enums, security schemes, and examples.
- Define pagination and sorting for list endpoints.
- Validate every body, path parameter, and query parameter.
- Use consistent error codes.
- Redact personal data and secrets from logs.
- Add contract tests for critical endpoints.
- Document idempotency behavior and retry semantics.
- Never expose internal provider secrets or another customer's records.
