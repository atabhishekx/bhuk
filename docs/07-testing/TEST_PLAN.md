# Test Plan

**Status:** Initial plan.

## 1. Objectives

Verify that ordering, payment, restaurant processing, delivery assignment, and tracking work correctly under normal, invalid, concurrent, and failure conditions. Verify that users cannot access unauthorized data or operations.

## 2. Test levels

### Unit tests
- Price calculation and rounding.
- Modifier selection validation.
- Delivery serviceability and fee rules.
- Allowed order/payment/assignment transitions.
- Partner eligibility and ranking.
- Cancellation and refund eligibility.

### Integration tests
- Database transactions and constraints.
- Payment provider sandbox and webhook verification.
- Notification provider retries.
- Maps/geocoding integration if enabled.
- Outbox/queue processing if implemented.

### API tests
- Required fields and malformed values.
- Authentication and role permissions.
- Outlet/customer resource scoping.
- Pagination and filtering.
- Idempotency and conflict responses.
- Rate limiting and error format.

### End-to-end tests
- Browse → customize → cart → checkout → order confirmation.
- Restaurant accepts → prepares → marks ready.
- Partner accepts → picks up → completes delivery.
- Customer views order history and status.
- Cancellation and refund path.
- Admin updates item availability and customer menu reflects it.

### Security and resilience tests
- Cross-customer and cross-outlet access attempts.
- Forged/replayed payment webhooks.
- Duplicate checkout requests.
- Provider timeout and delayed callback.
- Notification outage.
- Partner disconnection and assignment timeout.
- Concurrent partner acceptance.
- Database backup restoration.

## 3. Critical scenarios

| ID | Scenario | Expected result |
|---|---|---|
| T-001 | Customer adds available item and valid modifiers | Cart shows correct selection |
| T-002 | Required modifier missing | Request rejected with actionable validation error |
| T-003 | Item becomes unavailable before checkout | Checkout is blocked and customer is informed |
| T-004 | Client tampers with price | Server recalculates; tampered amount is ignored/rejected |
| T-005 | Customer submits order twice with same idempotency key | One logical order and no duplicate payment effect |
| T-006 | Payment provider callback is replayed | Callback is idempotent; no repeated side effect |
| T-007 | Payment redirect succeeds but provider confirmation is pending | UI does not falsely claim payment success |
| T-008 | Staff accesses order from unauthorized outlet | Access denied |
| T-009 | Customer requests another customer's order | Access denied or safely concealed |
| T-010 | Two partners accept the same delivery concurrently | Only one active assignment is committed |
| T-011 | Partner declines or offer expires | Attempt recorded; safe reassignment attempted |
| T-012 | No partner is available | Order appears in unassigned operations queue |
| T-013 | Partner marks pickup before order is ready | Transition rejected |
| T-014 | Notification service is unavailable | Order state remains committed; notification retries/logs |
| T-015 | Delivery fails | Reason is recorded and operations can investigate |
| T-016 | Admin changes menu availability | Change is audited and reflected in ordering |
| T-017 | Invalid state transition is submitted | Server rejects transition |
| T-018 | Refund request is duplicated | Provider/idempotency strategy prevents duplicate refund effect |

## 4. Test data

- Use synthetic customer profiles, addresses, and payment references.
- Use provider sandbox accounts for payment testing.
- Create fixtures for multiple outlets, active/inactive items, zones, partners, and order states.
- Never copy production personal/payment data into development or CI.

## 5. Automation and CI

For each pull request, run available:
- Formatting/lint.
- Static type checks.
- Unit tests.
- Integration tests appropriate for CI.
- Build.
- Dependency/security checks where configured.

Run critical end-to-end and staging integration tests before release. Keep test commands documented in `README.md`.

## 6. Exit criteria

- All P0 acceptance tests pass.
- No open critical/high security defects without explicit risk acceptance.
- Payment and assignment idempotency are verified.
- Database migration has been tested.
- Backup/restore and operational alerts are verified.
- Known limitations and test evidence are documented.
