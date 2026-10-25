# Business Rules

**Status:** Draft — business-owner confirmation required.

## 1. Chain and outlet model

- BR-01: The platform represents one restaurant chain, not unrelated restaurants.
- BR-02: An order belongs to exactly one outlet.
- BR-03: The customer cart contains items from one outlet only.
- BR-04: If the customer changes the selected outlet, the system must warn that cart contents may need to be cleared or revalidated.
- BR-05: Outlet selection uses configured serviceability, operating hours, item availability, and capacity rules.

## 2. Menu and modifiers

- BR-10: Only active and available items may be purchased.
- BR-11: Required modifiers must be selected before checkout.
- BR-12: Modifier quantities and allowed combinations follow each modifier group's configuration.
- BR-13: Historical orders retain item names, selected modifiers, and prices as snapshots.
- BR-14: Menu edits after order placement do not rewrite historical order records.

## 3. Price calculation

The backend computes the final total using a documented calculation order. Suggested order:

1. Validate item availability and current unit prices.
2. Calculate each line's base price plus selected modifier price deltas.
3. Multiply line price by quantity.
4. Apply eligible discounts according to defined stacking rules.
5. Calculate applicable taxes according to approved tax rules.
6. Add delivery and other disclosed fees.
7. Apply any approved rounding rule.
8. Return a line-itemized total to the client.

The exact tax basis, discount order, rounding mode, delivery fee, minimum order, and platform/service fee policies are **TBD** and must be approved before production.

## 4. Checkout and order creation

- BR-20: The server is authoritative for every monetary value.
- BR-21: A cart must be non-empty, valid, serviceable, and associated with one outlet.
- BR-22: Order submission uses an idempotency key or equivalent duplicate-prevention strategy.
- BR-23: The order stores the address and order-item snapshots used at purchase.
- BR-24: Payment success is confirmed only through a trusted server-side provider flow.
- BR-25: If payment is pending, the UI must not falsely claim the order is paid.
- BR-26: Payment and order status are independent, with reconciliation for mismatches.

## 5. Order acceptance and preparation

- BR-30: Only authorized staff assigned to the outlet may process its orders.
- BR-31: A new order may be accepted or rejected according to the configured response window.
- BR-32: Rejection requires a reason from an approved list or a recorded explanation.
- BR-33: Staff can move an accepted order through preparation states only in permitted sequence.
- BR-34: An order cannot be picked up before it is marked ready and an assignment is confirmed.
- BR-35: Cancellation eligibility depends on the order state and published cancellation policy.

## 6. Delivery assignment

- BR-40: Only active partners who are available and eligible for the service area may receive offers.
- BR-41: A delivery can have at most one active assignment.
- BR-42: A partner's acceptance and assignment reservation must be atomic to avoid competing claims.
- BR-43: Declined and expired offers are recorded and may be retried with another eligible partner.
- BR-44: If no partner is available, the order remains visible in an unassigned operations queue.
- BR-45: Manual assignment or reassignment requires authorization and an audit reason.
- BR-46: Delivery completion is recorded by an authorized partner or approved operations override.

## 7. Cancellation, refund, and failure

- BR-50: Cancellation policy must define who can cancel at each order state.
- BR-51: Refund eligibility and timing depend on payment provider capability and approved business policy.
- BR-52: Refund requests are not considered complete until provider-confirmed status is recorded.
- BR-53: A failed delivery requires a reason and operational follow-up.
- BR-54: Payment, notification, or mapping provider outages must not silently corrupt order state.

## 8. Data and audit

- BR-60: Every important order, payment, assignment, cancellation, and administrative transition is auditable.
- BR-61: Logs must not contain secrets, full payment credentials, or unnecessary personal data.
- BR-62: Personal-data retention and deletion periods require business/legal approval.
- BR-63: All displayed fees and applicable taxes must be disclosed before order placement.

## 9. Decisions to approve

- Minimum order amount.
- Delivery fee model (flat, zone-based, distance-based, or hybrid).
- Tax and rounding rules.
- Discount/coupon stacking.
- Order acceptance timeout.
- Partner offer timeout and maximum assignment attempts.
- Customer cancellation cut-off.
- Refund policy and escalation process.
- Delivery proof requirements (OTP, photo, signature, or none).
- Cash-on-delivery availability and cash reconciliation.
