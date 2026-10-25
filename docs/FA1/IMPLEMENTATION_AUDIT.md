# Implementation Audit - Bhuk Food Delivery System

## Repository Overview
- **Project Name**: Restru (also referred to as Bhuk in frontend)
- **Type**: Single-restaurant-chain food delivery application
- **Architecture**: Full-stack (React frontend + Spring Boot backend)
- **Database**: MySQL with Flyway migrations

## 1. Technology Stack Verification

### Backend (Verified)
| Technology | Version | Status |
|------------|---------|--------|
| Java | 17 | ✅ Confirmed in pom.xml |
| Spring Boot | 3.2.5 | ✅ Parent POM |
| Spring Data JPA | 3.2.5 | ✅ Dependency |
| Spring Security | 6.2.4 | ✅ Dependency |
| MySQL Connector/J | 8.3.0 | ✅ Runtime dependency |
| HikariCP | 5.0.1 | ✅ Transitive |
| Flyway | 9.22.3 | ✅ Core + MySQL + Maven plugin |
| JJWT | 0.12.3 | ✅ API + impl + jackson |
| Lombok | Latest | ✅ Optional dependency |
| Bean Validation | 3.2.5 | ✅ Starter |

### Frontend (Verified)
| Technology | Version | Status |
|------------|---------|--------|
| React | 19.3.1 | ✅ package.json |
| TypeScript | ~6.0.3 | ✅ package.json |
| Vite | 8.3.4 | ✅ package.json |
| React Router DOM | 7.18.4 | ✅ package.json |
| Axios | 1.20.0 | ✅ package.json |
| Tailwind CSS | 4.3.3 | ✅ package.json |
| oxlint | 1.88.0 | ✅ dev dependency |

## 2. Project Structure

### Backend Package Structure
```
com.restru
├── RestruApplication.java           # Entry point
├── auth/                            # Authentication & User Management
│   ├── controller/ (AuthController, AdminUserController)
│   ├── service/ (AuthService, UserService)
│   ├── repository/ (UserRepository, UserRoleRepository, OutletRepository, UserAddressRepository)
│   ├── entity/ (User, UserRole, UserAddress, Outlet)
│   ├── dto/ (AuthRequest, AuthResponse, RegisterRequest, UserProfileResponse, AddressRequest, AddressResponse, AssignRoleRequest)
│   └── security/ (JwtUtil, JwtAuthenticationFilter, CustomUserDetailsService, SecurityConfig, UserDetailsImpl)
├── menu/                            # Menu Management
│   ├── controller/MenuController
│   ├── service/MenuService
│   ├── repository/ (MenuCategoryRepository, MenuItemRepository, ModifierGroupRepository, ModifierOptionRepository, OutletItemAvailabilityRepository)
│   ├── entity/ (MenuCategory, MenuItem, ModifierGroup, ModifierOption, ItemModifierGroup, ItemModifierGroupId, OutletItemAvailability, OutletItemAvailabilityId)
│   └── dto/ (CategoryRequest/Response, ItemRequest/Response, ModifierGroupRequest/Response, ModifierOptionRequest/Response, ServiceabilityResponse)
├── cart/                            # Shopping Cart
│   ├── controller/CartController
│   ├── service/CartService
│   ├── repository/ (CartRepository, CartItemRepository, CartItemModifierRepository)
│   ├── entity/ (Cart, CartItem, CartItemModifier)
│   └── dto/ (AddToCartRequest, CartResponse, CartItemResponse, CartItemModifierResponse, CheckoutQuoteResponse)
├── order/                           # Order Lifecycle & Payments
│   ├── controller/OrderController
│   ├── service/OrderService, OrderNumberGenerator
│   ├── repository/ (OrderRepository, OrderItemRepository, OrderItemModifierRepository, OrderStatusEventRepository, IdempotencyRecordRepository)
│   ├── entity/ (Order, OrderItem, OrderItemModifier, OrderStatusEvent, IdempotencyRecord)
│   └── dto/ (CreateOrderRequest, OrderResponse, OrderItemResponse, OrderItemModifierResponse, OrderStatusEventResponse, OperationsReportResponse)
├── delivery/                        # Delivery Partner Tracking
│   ├── controller/DeliveryController
│   ├── service/DeliveryService
│   ├── repository/ (DeliveryPartnerRepository, DeliveryAssignmentRepository, DeliveryIssueRepository, DeliveryTrackingRepository)
│   ├── entity/ (DeliveryPartner, DeliveryAssignment, DeliveryIssue, DeliveryTracking)
│   └── dto/ (AssignPartnerRequest, DeliveryAssignmentResponse, DeliveryPartnerResponse, DeliveryTrackingResponse, OrderTrackingResponse, UpdateLocationRequest, SetAvailabilityRequest, ReportIssueRequest)
├── common/                          # Shared components
│   ├── dto/ (ApiResponse, ErrorResponse)
│   ├── exception/
│   └── controller/ (HealthController, WelcomeController)
└── config/                          # Configuration
    └── WebConfig
```

### Frontend Structure
```
frontend/frontend/src/
├── main.tsx                          # Entry point
├── App.tsx                           # Routes + Providers
├── api/axios.ts                      # Axios instance with interceptors
├── contexts/AuthContext.tsx          # Global auth state
├── pages/                            # Page components
│   ├── Login.tsx, Register.tsx
│   ├── Menu.tsx, Cart.tsx, Checkout.tsx
│   ├── Orders.tsx, OrderTracking.tsx
│   ├── Profile.tsx
│   ├── admin/ (AdminDashboard, AdminMenu, AdminOrders)
│   ├── DeliveryGuyDashboard.tsx
│   ├── StaffDashboard.tsx, StaffQueue.tsx
│   └── PartnerDashboard.tsx
└── components/
    ├── Navbar.tsx
    ├── CartItem.tsx
    ├── Common.tsx                    # Reusable UI
    ├── Drawer.tsx
    ├── PageLayout.tsx
    └── Toast.tsx
```

## 3. Database Migrations (Flyway)

| Version | File | Description | Status |
|---------|------|-------------|--------|
| V1 | V1__baseline_schema.sql | Users, roles, outlets, outlet_hours | ✅ Applied |
| V2 | V2__seed_dev_data.sql | Dev users (admin, delivery, customer) | ✅ Applied |
| V3 | V3__menu_schema.sql | Categories, items, modifiers, availability | ✅ Applied |
| V4 | V4__cart_schema.sql | Carts, cart_items, cart_item_modifiers | ✅ Applied |
| V5 | V5__seed_menu_data.sql | 7 categories, 50 items with images | ✅ Applied |
| V6 | V6__order_schema.sql | Orders, items, payments, idempotency | ✅ Applied |
| V7 | V7__delivery_schema.sql | Partners, assignments, tracking, issues | ✅ Applied |
| V8 | V8__user_addresses_schema.sql | User addresses | ✅ Applied |
| V9 | V9__menu_item_image_and_dietary_seed.sql | Image URLs + dietary types | ✅ Applied |

**Total Tables**: 22 tables created across migrations

## 4. API Endpoints Implemented

### Public Endpoints (No Auth)
- `GET /api/v1/health` - Health check
- `GET /api/v1/menu/categories` - List categories
- `GET /api/v1/menu/items` - List items (filterable)
- `GET /api/v1/menu/items/{itemId}` - Get item details
- `GET /api/v1/menu/outlets/serviceability` - Check delivery serviceability
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/webhooks/payments/**` - Payment webhooks

### Protected Endpoints (JWT Required)

#### Customer
- `GET /api/v1/cart` - View cart
- `POST /api/v1/cart/items` - Add to cart
- `PUT /api/v1/cart/items/{id}/quantity` - Update quantity
- `DELETE /api/v1/cart/items/{id}` - Remove item
- `DELETE /api/v1/cart` - Clear cart
- `GET /api/v1/cart/checkout/quote` - Checkout quote
- `POST /api/v1/orders` - Create order (idempotent)
- `GET /api/v1/orders` - List own orders
- `GET /api/v1/orders/{orderId}` - View order detail
- `POST /api/v1/orders/{orderId}/cancel` - Cancel order
- `POST /api/v1/orders/{orderId}/payment-intent` - Initiate payment
- `GET /api/v1/orders/{orderId}/payment` - Payment state
- `GET /api/v1/me` - Current user profile

#### Admin (hasRole('ADMIN'))
- `POST /api/v1/menu/admin/categories` - Create category
- `PUT/DELETE /api/v1/menu/admin/categories/{id}` - Update/delete category
- `POST /api/v1/menu/admin/items` - Create item
- `PUT/DELETE /api/v1/menu/admin/items/{id}` - Update/delete item
- `POST /api/v1/menu/admin/modifier-groups` - Create modifier group
- `POST /api/v1/menu/admin/modifier-groups/{id}/options` - Create modifier option
- `POST /api/v1/auth/admin/assign-role` - Assign role to user
- `GET /api/v1/admin/orders` - Search/filter all orders
- `GET /api/v1/admin/reports/operations` - Operations report

#### Staff (hasRole('ADMIN') - currently same as admin)
- `POST /api/v1/staff/orders/{orderId}/accept` - Accept order
- `POST /api/v1/staff/orders/{orderId}/reject` - Reject order
- `POST /api/v1/staff/orders/{orderId}/status` - Advance status
- `POST /api/v1/staff/orders/{orderId}/handoff` - Handoff to delivery
- `GET /api/v1/staff/orders` - Outlet-scoped order queue

#### Delivery (hasRole('DELIVERY_GUY'))
- `GET /api/v1/delivery/available` - Available orders
- `POST /api/v1/delivery/assign` - Assign to self
- `POST /api/v1/delivery/orders/{orderId}/pickup` - Mark picked up
- `POST /api/v1/delivery/orders/{orderId}/delivered` - Mark delivered
- `POST /api/v1/delivery/orders/{orderId}/location` - Update location
- `POST /api/v1/delivery/availability` - Set availability
- `POST /api/v1/delivery/issues` - Report issue

## 5. Implemented vs Partially Implemented vs Missing

### ✅ Fully Implemented
1. **User Authentication** - JWT login/register, role-based access, token refresh via /me
2. **Menu Browsing** - Categories, items with images, dietary types, availability
3. **Shopping Cart** - Add/update/remove items, modifiers support, server-side pricing
4. **Order Creation** - Idempotent creation, address capture, quote validation
5. **Order State Machine** - PLACED → CONFIRMED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED
6. **Order History** - Customer and admin views
7. **Database Schema** - Complete with FKs, constraints, indexes
8. **Flyway Migrations** - 9 versions, clean + migrate works
8. **Frontend Pages** - All major pages exist with routing

### ⚠️ Partially Implemented
1. **Delivery Workflow** - Backend complete, frontend DeliveryGuyDashboard exists but needs integration testing
2. **Admin Menu Management** - AdminMenu.tsx exists, needs full CRUD verification
3. **Payment Integration** - Razorpay config exists, webhook endpoint exists, but no actual payment flow
4. **Role-based UI** - Frontend has role checks but some pages accessible without proper guards
5. **Staff Dashboard** - StaffDashboard.tsx and StaffQueue.tsx exist but role is ADMIN not OUTLET_STAFF

### ❌ Missing / Not Implemented
1. **Outlet Staff Role** - OUTLET_STAFF role defined but no separate staff login flow
2. **Payment Gateway** - No Razorpay client integration, no payment UI
3. **Notifications** - No email/SMS/push notifications
4. **Real-time Tracking** - No WebSocket for live order tracking
5. **Order Rating/Review** - No feedback system
6. **Promotions/Coupons** - No discount system
7. **Multi-outlet Customer Selection** - Single outlet hardcoded in seed
8. **Automated Tests** - No backend tests found, no frontend tests configured

## 6. Configuration & Startup

### Backend Environment Variables
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="restru_db"
$env:DB_USERNAME="root"
$env:DB_PASSWORD=""
$env:JWT_SECRET="dev-secret-key-at-least-32-characters-long"
$env:SERVER_PORT="8080"
```

### Backend Start Command
```powershell
cd C:\xampp\htdocs\Projects\College\restru\backend
& "C:\Users\abhiv\tools\apache-maven-3.9.15\bin\mvn.cmd" spring-boot:run
```

### Frontend Start Command
```powershell
cd C:\xampp\htdocs\Projects\College\restru\frontend\frontend
npm install
npm run dev
# Opens http://localhost:5173
```

### Database Reset
```powershell
cd C:\xampp\htdocs\Projects\College\restru\backend
& "C:\Users\abhiv\tools\apache-maven-3.9.15\bin\mvn.cmd" flyway:clean flyway:migrate
```

## 7. Test Credentials (After Seeding)

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@restru.local | admin123 |
| Delivery | delivery@restru.local | admin123 |
| Customer | customer@restru.local | admin123 |

## 8. Known Issues / Technical Debt

1. **Frontend API Base URL**: Uses `/api/v1` but Vite proxy not configured - needs proxy config for dev
2. **SecurityConfig**: Staff endpoints use `hasRole('ADMIN')` instead of `hasRole('OUTLET_STAFF')`
3. **OrderController**: `/staff/orders` endpoint checks ADMIN role, not outlet-scoped staff
4. **CartService**: No validation for max quantity per item
5. **Menu Items**: V5 seed removed original 4 categories, V9 adds 7 new ones - old categories gone
6. **Error Handling**: Some frontend errors show raw exceptions
7. **Type Safety**: Frontend uses `any` type extensively in Cart.tsx, Checkout.tsx
8. **No Test Suite**: Zero automated tests in backend or frontend

## 9. Module Selected for FA1 Implementation

**Customer Menu → Cart → Order Creation Workflow**

Rationale:
- Complete end-to-end flow exists in codebase
- Demonstrates frontend-backend integration
- Covers authentication, API calls, database persistence
- Core customer-facing functionality
- Can be verified with running application

## 10. Files to Focus for FA1 Evidence

### Backend
- `MenuController.java` - Public menu API
- `CartController.java` - Cart operations
- `OrderController.java` - Order creation
- `MenuService.java`, `CartService.java`, `OrderService.java` - Business logic
- V3, V4, V5, V6, V9 migrations - Database schema

### Frontend
- `Menu.tsx` - Menu browsing
- `Cart.tsx` - Cart management
- `Checkout.tsx` - Order placement
- `AuthContext.tsx` - Authentication state
- `axios.ts` - API client

### Database
- `menu_categories`, `menu_items`, `carts`, `cart_items`, `orders`, `order_items` tables