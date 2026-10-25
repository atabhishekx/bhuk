# UI/UX Specification

**Status:** UX baseline; visual design system and channel strategy are TBD.

## 1. Experience goals

- Make food discovery and checkout easy on mobile.
- Show availability and full cost before the customer commits.
- Keep order progress understandable and timely.
- Make restaurant operations usable during busy periods.
- Make delivery-partner actions quick, legible, and safe while stationary.
- Provide clear error, loading, empty, retry, and offline states.

## 2. Personas

### Customer
Wants to find a dish, customize it, pay, and know when it will arrive.

### Restaurant staff
Needs a prioritized order queue with clear timers, item modifiers, and simple preparation controls.

### Delivery partner
Needs clear assignment details, pickup/delivery actions, navigation handoff, and a way to report issues.

### Administrator/outlet manager
Needs oversight of orders, menu availability, outlets, delivery coverage, users, and exceptions.

## 3. Customer screens

1. Home/menu landing.
2. Outlet/serviceability selection if required.
3. Category listing and search.
4. Item details and modifier selection.
5. Cart.
6. Address selection and delivery eligibility.
7. Checkout, fees, and payment method.
8. Order confirmation.
9. Order tracking timeline.
10. Order history/details.
11. Profile and saved addresses.

## 4. Restaurant operations screens

- Staff sign-in.
- Incoming order queue with status and elapsed time.
- Order detail with item/modifier breakdown.
- Accept/reject flow with reason.
- Preparation workflow.
- Ready-for-pickup queue.
- Delivery handoff confirmation.
- Unassigned delivery queue and escalation indicator.

## 5. Delivery partner screens

- Sign-in and availability toggle.
- Assignment offer with accept/decline timer.
- Pickup details and outlet directions.
- Pickup confirmation.
- Delivery details and navigation handoff.
- Completion confirmation.
- Failed-delivery/issue form.
- Connection or location-permission status where applicable.

Do not encourage interaction with the app while driving. Delivery flows should prioritize safe, minimal interactions and comply with applicable operational rules.

## 6. Admin screens

- Operations dashboard.
- Menu/category/modifier management.
- Outlet and opening-hours management.
- Delivery zone and fee configuration.
- Order search and details.
- Partner assignment/reassignment.
- User and role management.
- Basic reports and audit history.

## 7. Component states

Every data-driven screen should account for:
- Initial loading.
- Empty results.
- Validation errors.
- Permission errors.
- Network/provider failure.
- Retryable vs non-retryable failure.
- Stale data.
- Success confirmation.
- Disabled/unavailable item.
- Session expiration.

## 8. Checkout requirements

- Display item subtotal, discounts, taxes, delivery fee, and total as separate lines where applicable.
- Revalidate availability and prices on the server.
- Clearly identify the selected delivery address and outlet.
- Prevent accidental duplicate submission while still supporting safe retries.
- If the total changes, show the revised amount and obtain confirmation where policy requires.
- Never show â€œpaidâ€ based only on a client-side redirect.

## 9. Tracking requirements

Use a readable status timeline with timestamp and short explanation. Show estimated delivery time only if the system can produce a reasonable estimate. Live map tracking is optional and requires a defined location-permission and privacy model.

## 10. Accessibility and responsiveness

- Design mobile-first.
- Maintain readable text, sufficient contrast, visible focus, keyboard navigation, and accessible labels.
- Do not communicate status by color alone.
- Provide adequate touch target sizes.
- Support screen readers on core flows.
- Target WCAG 2.2 AA where practical.

## 11. Visual design system to define

Before implementation, choose:
- Brand palette and semantic colors.
- Typography scale.
- Spacing and layout grid.
- Buttons, fields, cards, badges, dialogs, toasts, tables, and status components.
- Breakpoints and navigation patterns.
- Image aspect ratios and fallback behavior.
- Design tokens and reusable component naming.

## 12. UX validation

Test the main journeys with representative users:
- Place a valid order.
- Handle an unavailable item.
- Recover from payment timeout.
- Follow order progress.
- Process a busy restaurant queue.
- Accept and complete a delivery.
- Reassign a delivery when the first partner declines.


