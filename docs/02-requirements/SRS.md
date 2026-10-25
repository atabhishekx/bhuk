# Software Requirements Specification (SRS)

**Product:** Single-Chain Food Delivery System  
**Version:** 1.0 draft  
**Status:** For review  
**Related document:** `README.md` and PRD

## 1. Purpose

Specify the system behavior required to support customer ordering, restaurant order processing, delivery-partner assignment, order tracking, and administration for one restaurant chain.

## 2. Scope

### In scope
- Customer accounts and saved addresses.
- Chain menu browsing, categories, item customization, and availability.
- Cart and server-calculated checkout.
- Delivery serviceability and outlet selection.
- Order creation, payment status, cancellation handling, and order history.
- Restaurant staff order queue and preparation workflow.
- Delivery partner availability, assignment, pickup, and completion.
- Notifications and operational administration.
- Audit trails and basic operational reporting.

### Out of scope for MVP
- Third-party restaurant marketplace.
- Multi-vendor commissions or settlements.
- Grocery/courier services.
- Advanced loyalty, AI recommendations, or route optimization.
- International currencies unless separately approved.

## 3. Actors

| Actor | Responsibilities |
|---|---|
| Customer | Browse, customize, order, pay, track, view own history |
| Restaurant staff | Accept/reject orders, update preparation, hand off orders |
| Delivery partner | Set availability, accept work, update pickup/delivery |
| Outlet manager | Monitor and operate assigned outlet |
| System administrator | Configure chain-wide settings, outlets, users, and policies |
| Support staff | Investigate order issues within assigned permissions |

All access must be enforced server-side and scoped to the actor's role and outlet.

## 4. Functional requirements

### Accounts
- SRS-AUTH-01: The system shall authenticate users using the approved sign-in method.
- SRS-AUTH-02: Customers shall manage their own profile and saved addresses.
- SRS-AUTH-03: Staff and delivery-partner access shall require an active account and appropriate role.
- SRS-AUTH-04: The system shall prevent users from accessing another customer's private order data.

### Menu
- SRS-MENU-01: The system shall display active categories and items belonging to the chain.
- SRS-MENU-02: An item shall support description, image, base price, availability, and optional modifier groups.
- SRS-MENU-03: Modifier validation shall enforce minimum/maximum selections and required options.
- SRS-MENU-04: The selected outlet's availability and price shall be applied where outlet-specific menus are enabled.
- SRS-MENU-05: Unavailable items shall not be orderable.

### Cart and checkout
- SRS-CART-01: Customers shall add, update, and remove cart items.
- SRS-CART-02: A cart shall be associated with one outlet at a time.
- SRS-CART-03: The server shall recalculate all line items, discounts, taxes, and fees at checkout.
- SRS-CART-04: Checkout shall fail safely if an item becomes unavailable or the cart is invalid.
- SRS-CART-05: The system shall show the full payable amount before order submission.
- SRS-CART-06: Order creation shall be idempotent for a given client idempotency key.

### Address and outlet
- SRS-ADDR-01: The system shall validate whether a delivery address is serviceable.
- SRS-ADDR-02: The system shall choose or confirm one eligible outlet according to configured rules.
- SRS-ADDR-03: The system shall explain when an address is outside service coverage.
- SRS-ADDR-04: The order shall preserve a snapshot of the delivery address used at purchase.

### Orders
- SRS-ORDER-01: A successfully created order shall have a unique identifier and human-readable order number.
- SRS-ORDER-02: The system shall enforce allowed order-state transitions.
- SRS-ORDER-03: Every important status transition shall record actor, time, prior state, new state, and reason where relevant.
- SRS-ORDER-04: Restaurant staff shall accept or reject new orders and record rejection reasons.
- SRS-ORDER-05: Authorized staff shall update preparation states.
- SRS-ORDER-06: Customers shall view current status and status history for their own orders.
- SRS-ORDER-07: Cancellation shall follow the configured policy and trigger required payment/refund handling.
- SRS-ORDER-08: A customer shall not be able to directly mark an order delivered.

### Payments
- SRS-PAY-01: Payment status shall be stored separately from order status.
- SRS-PAY-02: The backend shall verify payment outcomes through the trusted provider response/webhook.
- SRS-PAY-03: Duplicate provider callbacks shall not duplicate payment effects.
- SRS-PAY-04: The system shall represent pending, succeeded, failed, and refunded/refund-pending states as applicable.
- SRS-PAY-05: The system shall not store raw card data.
- SRS-PAY-06: Payment/order mismatches shall be discoverable through logs and reconciliation.

### Delivery
- SRS-DEL-01: Active delivery partners shall be able to set availability.
- SRS-DEL-02: Assignment shall consider availability, service area, and configured workload constraints.
- SRS-DEL-03: A delivery shall have no more than one active assigned partner at a time.
- SRS-DEL-04: Declined or expired offers shall be eligible for safe reassignment.
- SRS-DEL-05: Partners shall record pickup and delivery outcomes.
- SRS-DEL-06: Failed delivery shall require a reason and create an audit event.
- SRS-DEL-07: Authorized staff shall be able to manually assign/reassign a partner with a reason.
- SRS-DEL-08: If no partner is available, the delivery shall enter an unassigned queue and alert operations staff.

### Administration
- SRS-ADMIN-01: Authorized administrators shall manage menu items, categories, modifiers, prices, and availability.
- SRS-ADMIN-02: Authorized administrators shall manage outlets, operating hours, service zones, and delivery fees.
- SRS-ADMIN-03: Administrators shall manage accounts and roles within permitted scope.
- SRS-ADMIN-04: Authorized users shall search orders using approved filters.
- SRS-ADMIN-05: Sensitive administrative actions shall be audited.

### Notifications
- SRS-NTF-01: The system shall notify customers of order placement and key order-state changes using configured channels.
- SRS-NTF-02: The system shall notify partners of new, expired, or changed assignments.
- SRS-NTF-03: Notification-provider failure shall not reverse a valid order-state change.
- SRS-NTF-04: Failed notifications shall be logged and retried where safe.

## 5. Core state models

### Order state
`PLACED â†’ ACCEPTED â†’ PREPARING â†’ READY_FOR_PICKUP â†’ PICKED_UP â†’ OUT_FOR_DELIVERY â†’ DELIVERED`

Possible terminal/exception states: `REJECTED`, `CANCELLED`, `DELIVERY_FAILED`.

The exact meaning of `PICKED_UP` versus `OUT_FOR_DELIVERY` must be standardized in implementation. Invalid transitions must be rejected.

### Payment state
`PENDING`, `AUTHORIZED` (if provider supports it), `SUCCEEDED`, `FAILED`, `REFUND_PENDING`, `REFUNDED`.

Payment states must not be conflated with order states.

### Assignment state
`UNASSIGNED`, `OFFERED`, `ASSIGNED`, `ACCEPTED`, `PICKED_UP`, `COMPLETED`, `DECLINED`, `EXPIRED`, `FAILED`, `CANCELLED`.

## 6. Non-functional requirements

- Performance: common menu/cart operations should generally respond within 2 seconds under normal expected load, excluding third-party latency.
- Status freshness: target customer-visible status update within 10 seconds under normal connectivity.
- Availability: initial target 99.5% monthly, to be validated against hosting budget.
- Security: server-side authorization, input validation, secure sessions, rate limits, secret management, and dependency updates.
- Privacy: data minimization, limited access, documented retention, and protected location data.
- Reliability: idempotent order/payment/assignment operations and recoverable failure handling.
- Accessibility: target WCAG 2.2 AA for primary customer flows where practical.
- Maintainability: automated tests, reviewed changes, migrations, and documented deployment steps.

## 7. External interfaces

- Payment gateway (provider TBD).
- Notification service (SMS/email/push/channel TBD).
- Maps/geocoding/distance service if required.
- Optional POS/kitchen display integration.
- Monitoring and error-reporting service.

## 8. Acceptance conditions

Release cannot proceed until critical flows are tested: menu-to-order, payment verification, restaurant processing, delivery assignment/reassignment, customer tracking, role-based access, and recovery from duplicate requests/provider timeouts.

## 9. Open decisions

1. Customer channel: responsive web/PWA, native app, or both?
2. Single outlet or multiple chain outlets at launch?
3. Guest checkout?
4. Payment methods/provider?
5. Delivery workforce and assignment model?
6. Service zones and fee rules?
7. Cancellation and refund policy?
8. Required notification channels?
9. Expected order volume and service hours?
10. Required integrations with existing restaurant systems?


