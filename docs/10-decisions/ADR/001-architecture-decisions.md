# ADR 001: Initial Architecture and Technology Decisions

- **Status:** Proposed; decisions not yet approved.
- **Date:** 2026-10-25
- **Decision owners:** Product owner and engineering lead.

## Context

The product is a food delivery system for one restaurant chain, possibly with multiple outlets. It needs reliable order creation, payment integration, restaurant workflows, partner assignment, and tracking. Early implementation should minimize unnecessary operational complexity while preserving clear domain boundaries.

## Proposed decisions

### 1. Start with a modular monolith

**Proposal:** Use one backend application organized into modules for identity, outlets/menu, cart/pricing, orders, payments, delivery, notifications, and administration.

**Rationale:** A modular monolith is usually easier for a small team to build, test, deploy, and transact across than a microservice architecture. Modules can be separated later if evidence justifies it.

**Revisit when:** Independent scaling, team ownership, or reliability boundaries make extraction beneficial.

### 2. Use a relational database for transactional data

**Proposal:** Use a relational database with constraints and transactions for orders, payments, menu, and assignments.

**Rationale:** These workflows need referential integrity, unique constraints, and atomic state changes.

**Revisit when:** A concrete workload or requirement justifies another storage model.

### 3. Keep money and state transitions server-authoritative

**Proposal:** All prices, discounts, fees, payment outcomes, and order transitions are validated server-side.

**Rationale:** Client state is untrusted; monetary and operational consistency is essential.

### 4. Keep order, payment, and delivery state separate

**Proposal:** Model separate state machines and audit events.

**Rationale:** An order can be awaiting acceptance while payment is pending, or a payment can succeed while order confirmation requires recovery.

### 5. Make critical operations idempotent

**Proposal:** Apply idempotency to order creation, provider callbacks, refunds where supported, and assignment claims.

**Rationale:** Retries and timeouts are normal in distributed systems. Safe retries prevent duplicate effects.

## Decisions still open

| Decision | Options | Required input |
|---|---|---|
| Client channel | Responsive web/PWA, Android/iOS, hybrid | Launch audience, budget, offline/device needs |
| Backend framework | To be selected | Team skills, ecosystem, deployment |
| Frontend framework | To be selected | UX needs, team skills, maintainability |
| Database | PostgreSQL or another relational database | Hosting and team familiarity |
| Authentication | Managed provider or first-party | Requirements, budget, identity flows |
| Payments | Provider suitable for launch market | Fees, supported methods, compliance |
| Notifications | SMS/email/push/messaging | Customer expectations, cost, consent |
| Live updates | Polling, SSE, WebSockets | Expected traffic and UX requirements |
| Delivery assignment | Manual, automatic, hybrid | Operational workflow and fleet size |
| Hosting | Managed platform/cloud | Budget, region, availability, operational expertise |

## Consequences

- Exact stack-specific commands and file structure must be added after choices are approved.
- API and schema contracts should be finalized before broad parallel implementation.
- Operational simplicity is preferred over premature microservices.
- Security, testing, observability, and migration discipline are required from the beginning.

## Approval

Record approver, date, final choice, and any deviations when this ADR is accepted. Create a new ADR for material changes rather than silently rewriting the historical rationale.


