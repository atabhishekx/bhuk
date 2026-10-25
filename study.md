# Restru - Study Guide for Project Evaluation

## Project Overview

**Restru** is a single-chain food delivery system with:
- **Backend**: Spring Boot 3.2.5 (Java 17) - REST API
- **Frontend**: React 18 + Vite + TypeScript + Tailwind CSS
- **Database**: MySQL (XAMPP) with Flyway migrations
- **Auth**: JWT-based stateless authentication
- **Architecture**: Layered (Controller → Service → Repository)

---

## 1. BACKEND ARCHITECTURE (Spring Boot)

### 1.1 Package Structure
```
com.restru
├── RestruApplication.java          # Entry point (@SpringBootApplication)
├── auth/                           # Authentication & User Management
│   ├── controller/                 # AuthController, AdminUserController
│   ├── service/                    # AuthService, UserService
│   ├── repository/                 # UserRepository, UserRoleRepository, etc.
│   ├── entity/                     # User, UserRole, UserAddress, Outlet
│   ├── dto/                        # AuthRequest, AuthResponse, RegisterRequest, etc.
│   └── security/                   # JWT, SecurityConfig, Filters
├── menu/                           # Menu Management (Categories, Items, Modifiers)
│   ├── controller/MenuController
│   ├── service/MenuService
│   ├── repository/
│   ├── entity/
│   └── dto/
├── cart/                           # Shopping Cart
├── order/                          # Order Lifecycle & Payments
├── delivery/                       # Delivery Partner Tracking
├── common/                         # Shared: DTOs, Exception Handling
│   ├── dto/ApiResponse, ErrorResponse
│   └── controller/HealthController, WelcomeController
└── config/WebConfig                # CORS, WebMvcConfigurer
```

### 1.2 Core Concepts Explained

#### **Controller** (`@RestController`)
- **Purpose**: HTTP layer - handles requests, returns responses
- **Annotations**: `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, etc.
- **Responsibility**: 
  - Request validation (`@Valid`)
  - Call service layer
  - Return `ResponseEntity<ApiResponse<T>>`
- **Example**: `MenuController` - GET `/api/menu/categories`, POST `/api/menu/items`

#### **Service** (`@Service`)
- **Purpose**: Business logic layer
- **Annotations**: `@Service`, `@Transactional`
- **Responsibility**:
  - Business rules, validation
  - Orchestrate multiple repositories
  - Transaction management
- **Example**: `MenuService` - `createCategory()`, `getItemsByCategory()`

#### **Repository** (extends `JpaRepository`)
- **Purpose**: Data access layer
- **Spring Data JPA** generates queries from method names
- **Custom queries**: `@Query` annotation
- **Example**: `MenuItemRepository` - `findByCategoryIdAndActiveTrue()`

#### **Entity** (`@Entity`, `@Table`)
- **Purpose**: Maps to database table
- **Annotations**:
  - `@Id`, `@GeneratedValue` - Primary key
  - `@Column` - Column mapping
  - `@ManyToOne`, `@OneToMany` - Relationships
  - `@Enumerated(EnumType.STRING)` - Enum as string
- **Example**: `MenuItem` → `menu_items` table

#### **DTO (Data Transfer Object)**
- **Purpose**: Decouples API from database schema
- **Request DTOs**: Input validation (`@NotNull`, `@Size`, `@Email`)
- **Response DTOs**: Shape API response (hide sensitive fields)
- **Pattern**: `*Request` for input, `*Response` for output
- **Example**: `CreateOrderRequest` vs `OrderResponse`

#### **ApiResponse<T>** (Wrapper)
```java
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String requestId;  // For tracing
}
```
- **Why**: Consistent response format, easier frontend handling

#### **GlobalExceptionHandler** (`@ControllerAdvice`)
- Centralized error handling
- Maps exceptions to HTTP status codes
- Returns `ErrorResponse` with `requestId` for debugging

---

### 1.3 Authentication & Security

#### **JWT Flow**
1. **Login** (`POST /api/auth/login`) → Returns `accessToken`
2. **Client stores** token in localStorage
3. **Each request** sends `Authorization: Bearer <token>`
4. **JwtAuthenticationFilter** validates token, sets `SecurityContext`

#### **Key Security Classes**
| Class | Purpose |
|-------|---------|
| `JwtUtil` | Generate/validate tokens, extract claims |
| `JwtAuthenticationFilter` | Intercepts requests, validates JWT |
| `CustomUserDetailsService` | Loads user by email for Spring Security |
| `SecurityConfig` | Configures filter chain, public/private endpoints |
| `UserDetailsImpl` | Wraps User entity for Spring Security |

#### **Roles** (in `UserRole` entity)
- `ADMIN` - Full access, chain-wide
- `USER` - Customer (place orders, view menu)
- `DELIVERY_GUY` - Assigned to outlet, delivery ops
- `OUTLET_STAFF` - Kitchen/order management

---

### 1.4 Database Design (Key Tables)

| Table | Purpose |
|-------|---------|
| `users` | Core user accounts |
| `user_roles` | Role assignments (user + role + outlet) |
| `outlets` | Restaurant branches |
| `menu_categories` | Categories per outlet |
| `menu_items` | Food items (price, image, dietary_type) |
| `modifier_groups` | Choice groups (spice level, add-ons) |
| `modifier_options` | Options within group |
| `carts` / `cart_items` | User shopping cart |
| `orders` / `order_items` | Order lifecycle |
| `payments` / `refunds` | Payment tracking |
| `delivery_partners` | Delivery personnel |
| `delivery_assignments` | Order → partner mapping |
| `user_addresses` | Saved addresses |

#### **Money Storage**
- All amounts in **minor units (paise)** = INR × 100
- `INT` columns: `base_price_minor`, `grand_total_minor`
- Avoids floating-point precision issues

---

### 1.5 Flyway Migrations (Versioned SQL)

Location: `src/main/resources/db/migration/`

| Version | Description |
|---------|-------------|
| V1 | Baseline schema (tables, FKs, indexes) |
| V2 | Seed dev data (outlet, admin, delivery, customer users) |
| V3 | Menu schema (categories, items, modifiers) |
| V4 | Cart schema |
| V5 | Seed menu data (7 categories, 50 items with images) |
| V6 | Order schema (orders, items, payments, idempotency) |
| V7 | Delivery schema (partners, assignments, tracking) |
| V8 | User addresses schema |
| V9 | Menu item images + dietary types |

**Commands**:
```bash
mvn flyway:clean flyway:migrate   # Reset + apply all
mvn flyway:repair                 # Fix failed migration checksum
mvn flyway:info                   # Show migration status
```

---

### 1.6 Key Design Patterns

| Pattern | Where Used |
|---------|------------|
| **Layered Architecture** | Controller → Service → Repository |
| **DTO Pattern** | All API boundaries |
| **Builder Pattern** | Lombok `@Builder` on DTOs |
| **Repository Pattern** | Spring Data JPA interfaces |
| **Filter Pattern** | `JwtAuthenticationFilter` |
| **Strategy Pattern** | Order status transitions |
| **Idempotency** | `idempotency_records` table + key on orders |

---

## 2. FRONTEND ARCHITECTURE (React + Vite)

### 2.1 Project Structure
```
frontend/
├── src/
│   ├── main.tsx                    # Entry point
│   ├── App.tsx                     # Routes + Providers
│   ├── api/axios.ts                # Axios instance with interceptors
│   ├── contexts/AuthContext.tsx    # Global auth state (React Context)
│   ├── pages/                      # Page components (route targets)
│   │   ├── Login.tsx, Register.tsx
│   │   ├── Menu.tsx, Cart.tsx, Checkout.tsx
│   │   ├── Orders.tsx, OrderTracking.tsx
│   │   ├── Profile.tsx
│   │   ├── admin/AdminDashboard.tsx, AdminMenu.tsx, AdminOrders.tsx
│   │   ├── DeliveryGuyDashboard.tsx
│   │   ├── StaffDashboard.tsx, StaffQueue.tsx
│   │   └── PartnerDashboard.tsx
│   └── components/
│       ├── Navbar.tsx
│       ├── CartItem.tsx
│       └── Common.tsx              # Reusable UI (Button, Input, Modal)
├── index.html
├── package.json
├── vite.config.ts
├── tailwind.config.js
└── tsconfig.json
```

### 2.2 Key Concepts

#### **Axios Instance** (`src/api/axios.ts`)
- Base URL: `http://localhost:8080`
- **Request Interceptor**: Attaches JWT from localStorage
- **Response Interceptor**: Handles 401 → redirect to login

#### **AuthContext** (`src/contexts/AuthContext.tsx`)
- **State**: `user`, `token`, `loading`
- **Methods**: `login()`, `register()`, `logout()`, `refreshUser()`
- **Persistence**: localStorage (token + user)
- **Usage**: `const { login, user, logout } = useAuth()`

#### **React Router v6**
- Routes defined in `App.tsx`
- **Protected Routes**: `<RequireAuth roles={['ADMIN']}>`
- **Role-based redirects** after login

#### **Tailwind CSS**
- Utility-first styling
- Custom colors in `tailwind.config.js` (`food-red`, `bhuk`)
- Responsive design mobile-first

---

### 2.3 Page ↔ API Mapping

| Page | API Calls |
|------|-----------|
| `Login.tsx` | `POST /api/auth/login` |
| `Register.tsx` | `POST /api/auth/register` |
| `Menu.tsx` | `GET /api/menu/categories`, `GET /api/menu/items` |
| `Cart.tsx` | `GET /api/cart`, `POST /api/cart/items`, `DELETE /api/cart/items/{id}` |
| `Checkout.tsx` | `POST /api/cart/quote`, `POST /api/orders` |
| `Orders.tsx` | `GET /api/orders`, `GET /api/orders/{id}` |
| `OrderTracking.tsx` | `GET /api/delivery/tracking/{orderId}` |
| `AdminMenu.tsx` | Full CRUD on categories/items/modifiers |
| `AdminOrders.tsx` | `GET /api/admin/orders`, `PATCH /api/admin/orders/{id}/status` |
| `DeliveryGuyDashboard.tsx` | `GET /api/delivery/available`, `POST /api/delivery/assign` |

---

## 3. COMMON EVALUATOR QUESTIONS & ANSWERS

### Q: "Why DTOs instead of returning Entities directly?"
**A**: 
- **Security**: Hide sensitive fields (password hash, internal IDs)
- **Decoupling**: API contract independent of DB schema changes
- **Performance**: Fetch only needed data (avoid N+1, lazy loading issues)
- **Validation**: Request DTOs use Bean Validation annotations

### Q: "How does JWT authentication work?"
**A**:
1. User logs in → server verifies password → generates signed JWT
2. JWT contains: `sub` (email), `roles`, `exp` (expiry), `iat` (issued at)
3. Client sends `Authorization: Bearer <token>` header
4. `JwtAuthenticationFilter` extracts token → validates signature & expiry
5. Loads user via `CustomUserDetailsService` → sets `SecurityContext`
6. Controller accesses `Principal` or `@AuthenticationPrincipal`

### Q: "Why store money in paise (minor units)?"
**A**: Floating-point (`double`/`float`) has precision errors (0.1 + 0.2 ≠ 0.3). Storing as integer paise avoids this. Display by dividing by 100.

### Q: "How do you handle concurrent orders / race conditions?"
**A**:
- **Optimistic Locking**: `@Version` on entities
- **Idempotency Keys**: Client sends unique key → server checks `idempotency_records` table
- **DB Constraints**: Unique constraints on `order_number`, `idempotency_key`
- **Transactional Services**: `@Transactional` on service methods

### Q: "Explain the order state machine."
**A**: 
```
PLACED → CONFIRMED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED
                    ↘ CANCELLED (any state before OUT_FOR_DELIVERY)
```
- Enforced in `OrderService` with `transitionTo()` validation
- Audit trail in `order_status_events` table

### Q: "How is delivery assignment handled?"
**A**:
1. Order reaches `READY_FOR_PICKUP`
2. Staff clicks "Assign Partner" → `POST /api/delivery/assign`
3. System finds available partner near outlet
4. Creates `DeliveryAssignment` + `DeliveryTracking`
5. Partner sees order in dashboard → accepts → updates location

### Q: "What is Flyway and why use it?"
**A**: Database migration tool. Version-controlled SQL scripts. Ensures:
- Same schema across dev/staging/prod
- Repeatable deployments
- Rollback capability (via `clean` + re-migrate)

### Q: "How do you handle CORS?"
**A**: `WebConfig` implements `WebMvcConfigurer`:
```java
registry.addMapping("/**")
    .allowedOrigins("http://localhost:5173")
    .allowedMethods("*")
    .allowCredentials(true);
```

### Q: "Explain the cart flow."
**A**:
1. User adds item → `POST /api/cart/items` (creates cart if needed)
2. Cart stored in DB linked to user
3. `GET /api/cart` returns items with computed totals
4. Checkout: `POST /api/cart/quote` → shows tax, delivery fee
5. `POST /api/orders` → creates order, clears cart

### Q: "How do modifiers work? (e.g., spice level, extra cheese)"
**A**:
- `ModifierGroup` (Spice Level) → `ModifierOption` (Mild/Medium/Spicy)
- Linked to items via `item_modifier_groups` join table
- Cart item stores selected options in `cart_item_modifiers`
- Price delta added to item price

### Q: "What is the purpose of `requestId` in responses?"
**A**: Distributed tracing. Generated per request (UUID). Logged in `GlobalExceptionHandler` and access logs. Helps correlate frontend errors with backend logs.

---

## 4. TECH STACK SUMMARY

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.2.5 | Application framework |
| Spring Data JPA | 3.2.5 | ORM / Repository abstraction |
| Spring Security | 6.2.4 | Auth & Authorization |
| MySQL Connector/J | 8.3.0 | DB driver |
| HikariCP | 5.0.1 | Connection pool |
| Flyway | 9.22.3 | DB migrations |
| JJWT | 0.12.3 | JWT handling |
| Lombok | Latest | Boilerplate reduction |
| Bean Validation | 3.2.5 | `@Valid`, constraints |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18 | UI library |
| TypeScript | 5.x | Type safety |
| Vite | 5.x | Build tool / dev server |
| React Router | 6.x | Client-side routing |
| Axios | 1.x | HTTP client |
| Tailwind CSS | 3.x | Styling |
| React Hook Form | 7.x | Form handling |

---

## 5. HOW TO RUN LOCALLY

### Prerequisites
- Java 17, Maven 3.9+, Node 18+, MySQL (XAMPP)

### Backend
```powershell
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:DB_HOST="localhost"; $env:DB_PORT="3306"; $env:DB_NAME="restru_db"
$env:DB_USERNAME="root"; $env:DB_PASSWORD=""
$env:JWT_SECRET="dev-secret-key-at-least-32-characters-long"
$env:SERVER_PORT="8080"
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# Opens http://localhost:5173
```

### Database Reset
```bash
cd backend
mvn flyway:clean flyway:migrate
```

---

## 6. TEST CREDENTIALS (After Seeding)

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@restru.local | admin123 |
| Delivery | delivery@restru.local | admin123 |
| Customer | customer@restru.local | admin123 |

---

## 7. KEY FILES TO KNOW FOR EVALUATION

| File | Why Important |
|------|---------------|
| `SecurityConfig.java` | Security filter chain, public endpoints |
| `JwtAuthenticationFilter.java` | Token validation per request |
| `CustomUserDetailsService.java` | User lookup for Spring Security |
| `GlobalExceptionHandler.java` | Centralized error responses |
| `AuthContext.tsx` | Frontend auth state management |
| `axios.ts` | API client with interceptors |
| `V5__seed_menu_data.sql` | Menu categories/items seed |
| `V9__menu_item_image_and_dietary_seed.sql` | 50 items with images |
| `OrderService.java` | Complex business logic (state machine) |

---

## 8. COMMON TROUBLESHOOTING

| Issue | Fix |
|-------|-----|
| `Access denied for user 'root'@'localhost'` | XAMPP MySQL root has **no password** → set `DB_PASSWORD=` |
| `Port 8080 already in use` | Kill process: `Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process -Force` |
| Flyway migration failed | `mvn flyway:clean flyway:migrate` (resets DB) |
| `Invalid default value for 'expires_at'` | MySQL requires default for NOT NULL TIMESTAMP → make nullable or add default |
| Frontend parse error `₹` | Typo for `??` (nullish coalescing) → fix in `.tsx` |

---

## 9. EXTENSION IDEAS (For Discussion)

1. **WebSocket** for real-time order tracking
2. **Redis** for session/cart caching
3. **Kafka** for event-driven order status updates
4. **Docker Compose** for full stack deployment
5. **Unit/Integration Tests** with Testcontainers
6. **OpenAPI/Swagger** for API documentation
7. **Payment Gateway** integration (Razorpay config exists)

---

## 10. GLOSSARY

| Term | Definition |
|------|------------|
| **JPA** | Java Persistence API - ORM standard |
| **Hibernate** | JPA implementation used by Spring Data |
| **BCrypt** | Password hashing algorithm (cost factor 10) |
| **Idempotency** | Safe to retry same request multiple times |
| **CORS** | Cross-Origin Resource Sharing |
| **DTO** | Data Transfer Object - API contract |
| **Entity** | JPA class mapped to DB table |
| **Repository** | Spring Data interface for DB ops |
| **Service** | Business logic layer |
| **Controller** | HTTP endpoint handler |
| **Flyway** | Database migration tool |
| **JWT** | JSON Web Token - stateless auth |
| **Paise** | 1/100 INR (minor currency unit) |

---

*Generated for Restru project evaluation preparation. Update as project evolves.*

---

## 11. TECHNICAL TERMS, DEFINITIONS & USE CASES

### 11.1 Architecture & Design Patterns
- **Layered Architecture (N-Tier)** — Separates presentation, business, and data access into layers. *Use:* Controller → Service → Repository. *Why:* Testability, maintainability, separation of concerns.
- **DTO (Data Transfer Object)** — Plain object carrying data across process boundaries. *Use:* API request/response bodies. *Why:* Decouples DB schema from API contract; hides sensitive fields.
- **Repository Pattern** — Abstracts data access behind an interface. *Use:* Spring Data JPA interfaces. *Why:* Swappable storage, easier testing.
- **Builder Pattern** — Constructs complex objects step-by-step. *Use:* Lombok `@Builder` on DTOs. *Why:* Readable object creation.
- **Filter Pattern (Interceptor)** — Pre-processes requests before reaching controllers. *Use:* `JwtAuthenticationFilter`. *Why:* Centralized auth.
- **Strategy Pattern** — Encapsulates interchangeable algorithms. *Use:* Order status transitions. *Why:* Clean state machine logic.
- **Idempotency** — Repeating a request yields same result. *Use:* Idempotency keys on orders. *Why:* Safe retries, prevents duplicate charges.
- **Optimistic Locking** — Detects concurrent updates via version column. *Use:* `@Version` on entities. *Why:* Avoids lost updates without DB locks.
- **State Machine** — Finite states with defined transitions. *Use:* Order lifecycle (PLACED → DELIVERED). *Why:* Prevents invalid status jumps.

### 11.2 Backend (Spring Boot) Terms
- **Spring Boot** — Opinionated framework for production-ready Spring apps. *Use:* REST API server. *Why:* Auto-config, embedded Tomcat, fast setup.
- **Spring MVC / @RestController** — Handles HTTP requests and returns JSON. *Use:* All controllers. *Why:* RESTful endpoints with minimal boilerplate.
- **Spring Data JPA** — Abstraction over JPA/Hibernate for DB operations. *Use:* Repository interfaces. *Why:* Auto-generated queries, less SQL.
- **Hibernate** — ORM mapping Java objects to DB tables. *Use:* Entity persistence. *Why:* Object-relational mapping, lazy loading.
- **JPA (Jakarta Persistence API)** — Standard ORM specification. *Use:* `@Entity`, `@OneToMany`. *Why:* Vendor-neutral persistence.
- **Bean Validation (JSR-380)** — Declarative validation via annotations. *Use:* `@Valid`, `@NotNull`, `@Email`. *Why:* Input validation at boundary.
- **Flyway** — Version-controlled DB migrations. *Use:* `V1__...sql` scripts. *Why:* Reproducible schema across environments.
- **JWT (JSON Web Token)** — Stateless signed token carrying claims. *Use:* Auth token. *Why:* No server-side sessions, scalable.
- **BCrypt** — Adaptive password hashing. *Use:* Password storage. *Why:* Resistant to brute force/rainbow tables.
- **CORS (Cross-Origin Resource Sharing)** — Browser security for cross-origin requests. *Use:* Allow frontend origin. *Why:* Enables SPA ↔ API communication.
- **Security Filter Chain** — Ordered filters for auth/authz. *Use:* `SecurityConfig`. *Why:* Centralized security.
- **@Transactional** — Wraps method in DB transaction. *Use:* Service write methods. *Why:* Atomicity, rollback on failure.
- **HikariCP** — High-performance JDBC connection pool. *Use:* DB connections. *Why:* Efficient resource reuse.
- **GlobalExceptionHandler (@ControllerAdvice)** — Centralized error handling. *Use:* Maps exceptions to HTTP responses. *Why:* Consistent error format.

### 11.3 Frontend (React) Terms
- **React** — Component-based UI library. *Use:* All pages/components. *Why:* Reusable UI, virtual DOM.
- **JSX** — Syntax extension for UI markup in JS. *Use:* Component templates. *Why:* Declarative, readable.
- **Hooks (useState, useEffect, useCallback)** — State and lifecycle in functional components. *Use:* Form state, data fetching. *Why:* No class components.
- **Context API** — Global state without prop drilling. *Use:* `AuthContext`. *Why:* Shared auth state.
- **React Router v6** — Client-side routing. *Use:* Page navigation. *Why:* SPA navigation without reload.
- **Controlled Components** — Form inputs driven by state. *Use:* Login/Register forms. *Why:* Single source of truth.
- **Axios** — HTTP client with interceptors. *Use:* API calls. *Why:* Request/response interceptors, simpler than fetch.
- **Vite** — Fast dev server and bundler. *Use:* Build tool. *Why:* HMR, fast builds.
- **Tailwind CSS** — Utility-first CSS framework. *Use:* Styling. *Why:* Rapid, consistent UI.
- **Responsive Design** — Adapts to screen sizes. *Use:* Grid/flex + breakpoints. *Why:* Mobile + desktop support.
- **Accessibility (a11y)** — Usable by all (ARIA, focus). *Use:* Labels, aria-invalid. *Why:* Inclusive UX.
- **Lazy Loading** — Defers loading resources. *Use:* Image `loading="lazy"`. *Why:* Performance.

### 11.4 Database Terms
- **Normalization** — Organizing data to reduce redundancy. *Use:* Separate tables (users, orders). *Why:* Data integrity.
- **Foreign Key (FK)** — Links rows across tables. *Use:* order → user. *Why:* Referential integrity.
- **Index** — Speeds up queries. *Use:* PK/FK columns. *Why:* Fast lookups.
- **Transaction (ACID)** — Atomic DB operations. *Use:* Order creation. *Why:* Consistency.
- **Minor Units (Paise)** — Store money as integers. *Use:* `base_price_minor`. *Why:* Avoids float precision errors.
- **Migration** — Versioned schema changes. *Use:* Flyway scripts. *Why:* Reproducible DB evolution.

### 11.5 API & Web Terms
- **REST (Representational State Transfer)** — Resource-based HTTP API style. *Use:* `/api/v1/menu/items`. *Why:* Standard, cacheable.
- **HTTP Methods** — GET/POST/PUT/DELETE/PATCH. *Use:* CRUD operations. *Why:* Semantic operations.
- **Status Codes** — 200 OK, 201 Created, 400 Bad Request, 401 Unauthorized, 404 Not Found. *Use:* API responses. *Why:* Client handling.
- **JSON** — Lightweight data format. *Use:* API payloads. *Why:* Language-agnostic.
- **Pagination** — Splitting large results. *Use:* Could be added. *Why:* Performance.
- **Caching** — Storing responses for reuse. *Use:* Browser/CDN. *Why:* Speed.

### 11.6 Security Terms
- **Authentication (AuthN)** — Verifying identity. *Use:* Login. *Why:* Know who the user is.
- **Authorization (AuthZ)** — Verifying permissions. *Use:* Role checks. *Why:* Restrict actions.
- **Role-Based Access Control (RBAC)** — Permissions by role. *Use:* ADMIN/USER/DELIVERY_GUY. *Why:* Scalable access control.
- **Bearer Token** — Token sent in Authorization header. *Use:* JWT. *Why:* Stateless auth.
- **HTTPS** — Encrypted HTTP. *Use:* Production. *Why:* Data confidentiality.
- **XSS/CSRF** — Web vulnerabilities. *Use:* Mitigations (sanitization, tokens). *Why:* Security.

### 11.7 DevOps & Tooling
- **Maven** — Java build tool. *Use:* Backend build. *Why:* Dependency management.
- **npm** — Node package manager. *Use:* Frontend deps. *Why:* JS ecosystem.
- **Git** — Version control. *Use:* Source tracking. *Why:* Collaboration/history.
- **Docker** — Containerization. *Use:* Future deployment. *Why:* Consistent environments.
- **CI/CD** — Automated build/test/deploy. *Use:* GitHub Actions. *Why:* Quality/speed.

### 11.8 UI/UX Terms
- **Component** — Reusable UI unit. *Use:* Navbar, Drawer. *Why:* Modularity.
- **State** — Data driving UI. *Use:* Form inputs. *Why:* Reactivity.
- **Props** — Inputs to components. *Use:* cartCount. *Why:* Reusability.
- **Animation/Transition** — Visual motion. *Use:* slide-up, pulse. *Why:* Engagement.
- **Skeleton Loading** — Placeholder while loading. *Use:* SkeletonCard. *Why:* Perceived performance.
- **Empty State** — UI when no data. *Use:* EmptyState component. *Why:* Guidance.
- **Toast Notification** — Brief feedback message. *Use:* Success/error alerts. *Why:* Non-blocking feedback.

### 11.9 How & Why (Quick Map)
- **Why JWT?** Stateless, scalable, no session store.
- **Why paise?** Integer math avoids float errors.
- **Why DTOs?** Security + decoupling.
- **Why Flyway?** Reproducible DB schema.
- **Why React Context?** Global auth without prop drilling.
- **Why Tailwind?** Fast, consistent styling.
- **Why layered architecture?** Maintainability/testability.
- **Why optimistic locking?** Concurrency safety.
- **Why idempotency keys?** Safe retries.

---

*End of study guide.*

---

## 12. QUICK DEFINITIONS (1–2 LINES EACH)

- **Controller** — Receives HTTP requests, calls services, returns responses. *Why:* Keeps web layer thin and separate from business logic.
- **Service** — Holds business rules and transactions. *Why:* Centralizes logic, reusable and testable.
- **Repository** — Talks to the database via Spring Data. *Why:* Hides SQL/JPA details from services.
- **Entity** — Java class mapped to a DB table. *Why:* Represents stored data as objects.
- **DTO** — Object used to send/receive API data. *Why:* Hides DB fields and shapes API contracts.
- **Why this folder structure?** — Grouping by feature (auth, menu, order) keeps related code together. *Why:* Easier navigation, scaling, and team work.
- **Auth** — Verifies who the user is (login). *Why:* Protects user accounts.
- **Authorization** — Checks what a user can do (roles). *Why:* Restricts admin/staff actions.
- **JWT** — A signed token sent with requests. *Why:* Stateless auth without server sessions.
- **BCrypt** — Hashes passwords safely. *Why:* Prevents password theft if DB leaks.
- **Flyway** — Runs versioned DB scripts. *Why:* Same schema everywhere, easy updates.
- **JPA/Hibernate** — Maps objects to tables automatically. *Why:* Less manual SQL.
- **Transaction** — All-or-nothing DB operation. *Why:* Keeps data consistent.
- **Paise (minor units)** — Stores money as integers. *Why:* Avoids decimal rounding errors.
- **React Component** — Reusable UI block. *Why:* Build UI from small pieces.
- **Hook (useState/useEffect)** — Adds state/lifecycle to components. *Why:* Simpler than class components.
- **Context** — Shares global state (auth). *Why:* Avoids passing props deeply.
- **Axios** — HTTP client for API calls. *Why:* Interceptors add JWT automatically.
- **Tailwind** — Utility CSS classes. *Why:* Fast, consistent styling.
- **Vite** — Dev server/bundler. *Why:* Fast refresh and builds.
- **REST API** — Resource-based HTTP endpoints. *Why:* Standard, easy to consume.
- **CORS** — Allows frontend to call backend. *Why:* Browser security for cross-origin.
- **Responsive** — Adapts to screen sizes. *Why:* Works on mobile and desktop.