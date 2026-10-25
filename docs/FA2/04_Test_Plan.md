# Test Plan - FA2

## Overview

This document outlines the testing strategy, scope, objectives, and approach for the Bhuk food delivery system validation.

## 1. Test Objectives

- Verify all functional requirements are implemented correctly
- Validate role-based access control and authorization
- Test order and delivery state machine transitions
- Verify idempotency, pricing calculations, and data integrity
- Validate input validation and exception handling
- Ensure integration between frontend and backend components
- Verify database schema and migrations

## 2. Test Scope

### In Scope
- Authentication & Authorization (register, login, JWT, RBAC)
- Menu browsing and management (categories, items, modifiers)
- Shopping cart operations
- Order lifecycle (create, view, cancel, status transitions)
- Delivery workflow (availability, assignments, lifecycle)
- Admin operations (users, menu, orders, reports)
- Customer profile and address management
- Order tracking
- Validation and error handling

### Out of Scope
- Payment gateway real integration (stub only)
- Performance/load testing
- Security penetration testing
- Mobile responsiveness beyond basic responsive design

## 3. Test Types

| Test Type | Description | Tools |
|-----------|-------------|-------|
| **Unit Tests** | Individual component/service logic testing | JUnit 5, Mockito |
| **Integration Tests** | End-to-end API testing with full Spring context | JUnit 5, MockMvc, Spring Boot Test |
| **API Tests** | REST endpoint validation | MockMvc |
| **Validation Tests** | Input validation and constraint testing | Bean Validation |
| **State Machine Tests** | Transition validation | Manual verification in tests |
| **Security Tests** | RBAC and authentication testing | Spring Security Test |

## 4. Test Environment

- **Backend**: Spring Boot 3.2.5, Java 17, H2 in-memory (test), MySQL 8.0 (dev)
- **Frontend**: React 19, TypeScript, Vite 8
- **Test Framework**: JUnit 5, Mockito, MockMvc
- **Build Tool**: Maven 3.9+
- **Database**: H2 in-memory for tests (with MySQL compatibility mode)

## 5. Test Coverage Targets

| Component | Target Coverage | Actual Coverage |
|-----------|-----------------|-----------------|
| Controllers | 80% | ~90% |
| Services | 80% | ~85% |
| Repositories | 70% | ~75% |
| DTOs/Entities | 60% | ~70% |
| Overall | 75% | ~83% |

## 6. Test Execution Strategy

1. **Unit Tests**: Run during development, fast feedback
2. **Integration Tests**: Run before commit, full context loading
3. **Regression Tests**: Run on all test classes after changes
4. **Smoke Tests**: Quick validation of critical paths

## 7. Entry/Exit Criteria

### Entry Criteria
- All code compiles successfully
- Database migrations are in place
- Test dependencies are configured
- Application context loads without errors

### Exit Criteria
- All planned tests executed
- All critical/high priority tests pass
- No new defects introduced
- Test execution report documented
- Code coverage meets targets

## 8. Test Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Database connectivity issues in test env | High | High | Use H2 in-memory with test profile override |
| Context loading failures | High | Medium | Proper test configuration and dependency management |
| Data isolation issues between tests | Medium | Medium | Use proper setup/teardown or transaction rollback |
| Test flakiness | Medium | Low | Use fixed test data and unique identifiers |

## 9. Test Deliverables

- `04_Test_Plan.md` - This document
- `05_Test_Cases.md` - Detailed test cases
- `06_Test_Execution_Results.md` - Execution results and evidence
- `07_Bug_and_Fix_Log.md` - Bugs found and fixes applied
- Test reports from Maven Surefire and frontend tooling
