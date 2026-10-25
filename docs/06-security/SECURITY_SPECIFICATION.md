# Security and Privacy Specification

**Status:** Baseline; validate against selected stack, providers, and applicable law.

## 1. Security objectives

Protect customer accounts and addresses, order integrity, payment flows, delivery-partner data, and administrative operations. Prevent unauthorized access, tampering, duplicate transactions, and exposure of secrets.

## 2. Authentication and sessions

- Use a maintained authentication mechanism.
- Store passwords only using a modern password-hashing algorithm if passwords are managed directly.
- Use secure session/token practices, expiration, revocation, and logout.
- Require appropriate stronger authentication for privileged accounts where feasible.
- Rate-limit sign-in, password recovery, OTP, and other abuse-prone endpoints.
- Avoid revealing whether an account exists during sensitive recovery flows.

## 3. Authorization

- Deny by default.
- Enforce permissions on the server for every protected operation.
- Customers can access only their own profiles, addresses, carts, and orders.
- Restaurant staff and managers are restricted to authorized outlets.
- Delivery partners can access only their own assignments and the minimum customer details needed for active delivery.
- Admin permissions should be separated by responsibility.
- Check resource ownership and role on every request; hiding UI controls is not authorization.

## 4. Payment security

- Use a reputable payment provider and its hosted/tokenized flow where possible.
- Never store raw card data.
- Verify webhook signatures and provider references server-side.
- Validate amount, currency, order reference, and expected payment state.
- Make webhook processing idempotent.
- Do not trust client redirects or client-submitted payment status.
- Protect refund actions with role checks, reason capture, audit logs, and reconciliation.

## 5. Input and application security

- Validate all request bodies, query parameters, IDs, uploads, and webhook payloads.
- Use parameterized database queries or safe ORM patterns.
- Protect against XSS, injection, CSRF where relevant, broken access control, SSRF where applicable, and unsafe file handling.
- Apply security headers and HTTPS.
- Configure CORS narrowly.
- Apply rate limits and request-size limits.
- Keep dependencies patched and scan for known vulnerabilities.
- Avoid exposing stack traces, secrets, or internal implementation details to clients.

## 6. Secrets and environments

- Store secrets in an approved secret manager or protected environment configuration.
- Keep `.env` files containing real credentials out of source control.
- Commit `.env.example` with placeholder values only.
- Use separate credentials for development, staging, and production.
- Rotate credentials after suspected exposure.
- Restrict production access and record privileged actions.

## 7. Privacy and location

- Collect only data required to deliver the service.
- Explain why location is requested and request permission where required.
- Avoid continuous partner-location collection when not needed for an active workflow.
- Restrict location access and define retention/deletion rules.
- Mask sensitive contact/address details in operational views when full detail is unnecessary.
- Define customer data access, correction, deletion, and retention processes with qualified legal review.

## 8. Logging and audit

Log security-relevant events such as failed authentication, role changes, order-state transitions, payment webhook outcomes, refunds, and manual assignment overrides.

Do not log:
- Passwords, authentication tokens, API keys, or session secrets.
- Full payment credentials.
- Unnecessary full addresses or location histories.
- Sensitive webhook payloads without redaction.

Use correlation IDs and timestamps; protect log access and retention.

## 9. Threat scenarios to test

- Customer changes an order ID to view another customer's order.
- Customer submits a lower price or fake discount.
- Partner tries to claim another partner's assignment.
- Staff updates an order outside their outlet.
- Forged payment webhook marks an unpaid order as paid.
- Duplicate webhook triggers repeated side effects.
- Replayed checkout request creates duplicate orders.
- Unauthorized admin creates a privileged account.
- Logs expose secrets or personal data.
- Brute-force attempts abuse authentication endpoints.

## 10. Security release gate

Before production:
- Review role/permission matrix.
- Test direct-object-reference and outlet-scope controls.
- Verify webhook signatures and idempotency.
- Scan dependencies and configuration.
- Confirm HTTPS, secret storage, backups, logging redaction, and access controls.
- Document incident response and credential rotation.
- Review applicable Indian privacy, consumer, tax, and payment obligations with qualified stakeholders.
