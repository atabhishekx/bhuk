# Problem Statement

## Food Delivery Problem

The food delivery market is dominated by multi-restaurant aggregators (Swiggy, Zomato) that charge high commissions (20-30%) from restaurants and prioritize volume over quality. Single-restaurant chains lack dedicated, affordable technology to manage their own delivery operations, forcing them to either:

1. **Build custom solutions** - Expensive, time-consuming, requires dedicated tech team
2. **Use aggregators** - High commissions, loss of customer relationship, no brand control
3. **Manual operations** - Phone/WhatsApp orders, error-prone, doesn't scale

**Pain Points for Single Chains:**
- No direct customer relationship (aggregator owns the customer)
- High per-order commissions erode margins
- No control over delivery experience or branding
- Menu changes require aggregator approval
- Customer data not accessible for loyalty/retention
- Delivery fleet management is manual or non-existent

## Proposed Solution: Bhuk (Restru)

**Bhuk** is a self-hosted, single-chain food delivery platform that gives restaurant chains complete ownership of their online ordering, delivery, and customer relationship.

### Core Value Proposition
- **Zero commission** - Restaurant keeps 100% of revenue
- **Full data ownership** - Customer database, order history, analytics
- **Brand control** - Custom domain, branding, customer experience
- **Integrated operations** - Menu, orders, kitchen, delivery in one system
- **Scalable** - Designed for single chain with multiple outlets

### Target Users
1. **Customers** - Browse menu, order, track delivery, reorder favorites
2. **Restaurant Staff** - Manage orders, update preparation status, kitchen queue
3. **Delivery Partners** - Accept orders, navigate, update location, mark delivered
4. **Administrators** - Manage menu, users, outlets, view reports

### Key Differentiators
- Single-chain focus (not multi-tenant)
- Self-hosted (data sovereignty)
- Role-based access control
- Real-time order tracking
- Idempotent order creation (no duplicate orders)
- Server-side price validation (no client-side price manipulation)