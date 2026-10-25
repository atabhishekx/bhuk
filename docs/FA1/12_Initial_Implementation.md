# Initial Implementation

## Module Selected for FA1
**Customer Menu → Cart → Order Creation Workflow**

This module demonstrates:
- Frontend ↔ Backend API integration
- Authentication (JWT) flow
- Database persistence (menu, cart, orders)
- Server-side price calculation
- Idempotent order creation
- Role-based access control

## Implementation Status

### ✅ Backend - Fully Implemented

| Component | File | Status |
|-----------|------|--------|
| Menu API | `MenuController.java` | ✅ Complete |
| Menu Service | `MenuService.java` | ✅ Complete |
| Menu Entities | `MenuCategory.java`, `MenuItem.java`, etc. | ✅ Complete |
| Cart API | `CartController.java` | ✅ Complete |
| Cart Service | `CartService.java` | ✅ Complete |
| Cart Entities | `Cart.java`, `CartItem.java`, `CartItemModifier.java` | ✅ Complete |
| Order API | `OrderController.java` | ✅ Complete |
| Order Service | `OrderService.java` | ✅ Complete |
| Order Entities | `Order.java`, `OrderItem.java`, etc. | ✅ Complete |
| Auth System | `AuthController`, `JwtUtil`, `SecurityConfig` | ✅ Complete |
| Migrations | V1-V9 | ✅ Applied |

### ✅ Frontend - Fully Implemented

| Page | File | Status |
|------|------|--------|
| Login | `Login.tsx` | ✅ Complete (fixed ₹→?? bug) |
| Register | `Register.tsx` | ✅ Complete |
| Menu | `Menu.tsx` | ✅ Complete |
| Cart | `Cart.tsx` | ✅ Complete |
| Checkout | `Checkout.tsx` | ✅ Complete |
| Orders | `Orders.tsx` | ✅ Complete |
| Order Tracking | `OrderTracking.tsx` | ✅ Complete |
| Auth Context | `AuthContext.tsx` | ✅ Complete |
| API Client | `axios.ts` | ✅ Complete |

### ✅ Database - Fully Implemented

| Table | Migration | Records (After Seed) |
|-------|-----------|---------------------|
| users | V1, V2 | 3 |
| outlets | V1, V2 | 1 |
| menu_categories | V3, V9 | 7 |
| menu_items | V3, V9 | 50 |
| modifier_groups | V3 | 3 |
| modifier_options | V3 | 8 |
| carts | V4 | 0 (created on demand) |
| cart_items | V4 | 0 |
| orders | V6 | 0 (created on order) |
| order_items | V6 | 0 |

## Key Implementation Details

### 1. Menu Browsing (`MenuController` + `Menu.tsx`)

**Backend Endpoints:**
```java
GET /api/v1/menu/categories          // List categories
GET /api/v1/menu/items               // List items (filter: outletId, categoryId)
GET /api/v1/menu/items/{itemId}      // Item detail
GET /api/v1/menu/outlets/serviceability  // Pincode check
```

**Frontend Flow:**
1. `Menu.tsx` loads categories + items on mount
2. Category filter via `selectedCategory` state
3. Items displayed in responsive grid with images
4. "Add" button calls `api.post('/cart/items', {itemId, quantity})`

### 2. Cart Management (`CartController` + `Cart.tsx`)

**Backend Endpoints:**
```java
GET    /api/v1/cart                    // View cart (authoritative pricing)
POST   /api/v1/cart/items              // Add item {itemId, quantity, modifiers}
PUT    /api/v1/cart/items/{id}/quantity // Update qty
DELETE /api/v1/cart/items/{id}         // Remove item
DELETE /api/v1/cart                    // Clear cart
GET    /api/v1/cart/checkout/quote     // Authoritative quote
```

**CartService Key Logic:**
- One cart per user (unique index on `customer_id`)
- Auto-sets `outlet_id` from first item added
- Validates item availability & outlet match
- **Server recalculates** all prices from DB (no client trust)
- Modifiers stored in `cart_item_modifiers`

**Frontend Features:**
- Quantity selector with +/- buttons
- Modifier display badges
- Real-time subtotal/tax/delivery/grand total
- "Clear Cart" button
- Navigate to Checkout

### 3. Order Creation (`OrderController` + `Checkout.tsx`)

**Backend Endpoints:**
```java
POST /api/v1/orders                              // Create order
GET  /api/v1/orders                              // List own orders
GET  /api/v1/orders/{orderId}                    // Order detail
POST /api/v1/orders/{orderId}/cancel             // Cancel (if eligible)
POST /api/v1/orders/{orderId}/payment-intent     // Payment (deferred)
```

**OrderService.createOrder() Flow:**
```java
1. Validate idempotency key (check idempotency_records)
2. Get user's cart
3. Validate cart not empty, items available
4. Recalculate prices from DB (menu_items + modifiers)
4. Create Order entity:
   - order_number: ORD-YYYYMMDD-XXXX
   - status: PLACED
   - delivery_address_json: full address snapshot
4. Create OrderItems with price snapshots
5. Create OrderItemModifiers
6. Create OrderStatusEvent (PLACED)
7. Store idempotency record
8. Clear cart (DELETE cart_items)
9. Return OrderResponse
```

**Idempotency Implementation:**
```java
// Client generates: `order-${Date.now()}-${Math.random()}`
// Server:
if (idempotencyRecordRepository.existsByRecordKey(key)) {
    // Verify request hash matches
    // Return cached response
}
// Process normally, store record with request hash
```

**Frontend Checkout Flow:**
1. `Checkout.tsx` loads cart + quote via `GET /cart` + `GET /cart/checkout/quote`
2. Address form: fullAddress, city, state, pincode, phone
3. On submit: `POST /orders` with idempotencyKey + deliveryAddress
4. Success → redirect to `/orders/{orderId}`

### 4. Authentication Flow

**Login (`Login.tsx` + `AuthController`):**
```typescript
// Frontend
const res = await api.post('/auth/login', {email, password})
const {token, user} = res.data.data
localStorage.setItem('bhuk_token', token)
setUser(user)

// Backend
@PostMapping("/auth/login")
public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );
    UserDetails userDetails = (UserDetails) auth.getPrincipal();
    String token = jwtUtil.generateToken(userDetails);
    return ResponseEntity.ok(ApiResponse.success(new AuthResponse(token, user)));
}
```

**Token Storage & Attachment:**
```typescript
// axios.ts interceptor
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('bhuk_token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})
```

**Protected Route Access:**
```typescript
// SecurityConfig.java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/auth/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/v1/menu/**").permitAll()
    .anyRequest().authenticated()
)
```

### 5. Database Persistence Verification

**After Order Creation:**
```sql
-- Check order created
SELECT * FROM orders WHERE customer_id = 3;

-- Check items with snapshots
SELECT * FROM order_items WHERE order_id = 1;

-- Check modifiers
SELECT * FROM order_item_modifiers WHERE order_item_id IN (SELECT id FROM order_items WHERE order_id = 1);

-- Check status audit
SELECT * FROM order_status_events WHERE order_id = 1;

-- Check idempotency
SELECT * FROM idempotency_records WHERE record_key LIKE 'order-%';

-- Check cart cleared
SELECT * FROM cart_items WHERE cart_id = (SELECT id FROM carts WHERE customer_id = 3);
```

## API Contracts (Request/Response)

### Add to Cart
```json
POST /api/v1/cart/items
{
  "itemId": 1,
  "quantity": 2,
  "modifierOptionIds": [5, 6]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "outletId": 1,
    "items": [
      {
        "id": 1,
        "itemId": 1,
        "itemName": "Butter Chicken",
        "quantity": 2,
        "unitPriceMinor": 32000,
        "modifiers": [
          {"id": 1, "modifierOptionId": 5, "modifierName": "Medium", "priceDeltaMinor": 0}
        ],
        "instructions": null
      }
    ],
    "itemCount": 2,
    "subtotalMinor": 64000,
    "taxMinor": 3200,
    "deliveryFeeMinor": 3000,
    "grandTotalMinor": 70200
  },
  "message": "Item added to cart."
}
```

### Checkout Quote
```json
GET /api/v1/cart/checkout/quote
```

**Response:**
```json
{
  "success": true,
  "data": {
    "subtotalMinor": 64000,
    "taxMinor": 3200,
    "deliveryFeeMinor": 3000,
    "grandTotalMinor": 70200,
    "itemCount": 2
  }
}
```

### Create Order
```json
POST /api/v1/orders
{
  "idempotencyKey": "order-1703512800000-abc123",
  "deliveryAddress": {
    "fullAddress": "123 Main St, Apt 4B",
    "city": "Bengaluru",
    "state": "Karnataka",
    "pincode": "560001",
    "phone": "+91-9999999999"
  },
  "deliveryAddressJson": "{\"fullAddress\":\"123 Main St, Apt 4B\",\"city\":\"Bengaluru\",\"state\":\"Karnataka\",\"pincode\":\"560001\",\"phone\":\"+91-9999999999\"}"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderNumber": "ORD-20241225-0001",
    "status": "PLACED",
    "subtotalMinor": 64000,
    "taxMinor": 3200,
    "deliveryFeeMinor": 3000,
    "grandTotalMinor": 70200,
    "deliveryAddressJson": "{...}",
    "items": [...],
    "createdAt": "2024-12-25T10:30:00Z"
  },
  "message": "Order created."
}
```

## Validation & Error Handling

### Backend Validation (DTOs)
```java
// AddToCartRequest.java
public class AddToCartRequest {
    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private List<Long> modifierOptionIds;
}

// CreateOrderRequest.java
public class CreateOrderRequest {
    @NotBlank(message = "Idempotency key is required")
    @Size(max = 128)
    private String idempotencyKey;

    @Valid
    @NotNull(message = "Delivery address is required")
    private DeliveryAddressRequest deliveryAddress;

    @NotBlank(message = "Delivery address JSON is required")
    private String deliveryAddressJson;
}
```

### Error Responses
```json
// 400 Validation Error
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {"field": "quantity", "message": "Quantity must be at least 1"}
    ]
  },
  "requestId": "req-abc123"
}

// 401 Unauthorized
{
  "success": false,
  "error": {
    "code": "AUTH_TOKEN_EXPIRED",
    "message": "Token expired or invalid"
  },
  "requestId": "req-def456"
}

// 409 Conflict (Idempotency)
{
  "success": false,
  "error": {
    "code": "IDEMPOTENCY_CONFLICT",
    "message": "Request already processed"
  },
  "requestId": "req-ghi789"
}
```

## Testing the Module

### Manual Test Script
```powershell
# 1. Login as customer
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/auth/login `
  -ContentType "application/json" -Body '{"email":"customer@restru.local","password":"admin123"}'
$token = $login.data.token

# 2. Browse menu
$categories = Invoke-RestMethod -Headers @{Authorization="Bearer $token"} http://localhost:8080/api/v1/menu/categories
$items = Invoke-RestMethod -Headers @{Authorization="Bearer $token"} http://localhost:8080/api/v1/menu/items

# 3. Add to cart
$cart = Invoke-RestMethod -Method Post -Headers @{Authorization="Bearer $token"; "Content-Type"="application/json"} `
  -Uri http://localhost:8080/api/v1/cart/items -Body '{"itemId":1,"quantity":2}'

# 4. Get quote
$quote = Invoke-RestMethod -Headers @{Authorization="Bearer $token"} http://localhost:8080/api/v1/cart/checkout/quote

# 5. Place order
$order = Invoke-RestMethod -Method Post -Headers @{Authorization="Bearer $token"; "Content-Type"="application/json"} `
  -Uri http://localhost:8080/api/v1/orders -Body (@"
{
  "idempotencyKey": "order-test-$(Get-Date -UFormat %s)-123",
  "deliveryAddress": {"fullAddress":"123 Test St","city":"Bengaluru","state":"KA","pincode":"560001","phone":"+91-9999999999"},
  "deliveryAddressJson": "{\"fullAddress\":\"123 Test St\",\"city\":\"Bengaluru\",\"state\":\"KA\",\"pincode\":\"560001\",\"phone\":\"+91-9999999999\"}"
}
"@)

# 6. Verify order in history
$orders = Invoke-RestMethod -Headers @{Authorization="Bearer $token"} http://localhost:8080/api/v1/orders
```

### Expected Results
| Step | Expected |
|------|----------|
| Login | 200, returns JWT token |
| Menu | 200, 7 categories, 50 items |
| Add to cart | 201, cart with item |
| Quote | 200, correct totals |
| Create order | 201, order_number generated |
| Order history | 200, includes new order |
| Duplicate order | 200, returns SAME order (idempotent) |

## Known Limitations (FA1 Scope)

1. **Payment Integration** - Razorpay config exists but no client SDK or payment UI
2. **Real-time Tracking** - Polling only, no WebSocket
3. **Notifications** - No email/SMS/push
3. **Multi-outlet** - Single outlet in seed, no customer outlet selection
4. **Staff Role** - OUTLET_STAFF role exists but no separate login UI
5. **Automated Tests** - Zero unit/integration tests
6. **Frontend Type Safety** - `any` types in Cart.tsx, Checkout.tsx
7. **Error Boundaries** - No React error boundaries
8. **Accessibility** - Basic only, not WCAG compliant

## Files Modified During FA1 Setup

| File | Change |
|------|--------|
| `V5__seed_menu_data.sql` | Removed 4 categories without images, kept modifier groups only |
| `V9__menu_item_image_and_dietary_seed.sql` | Fixed category_id references (1-7 instead of 5-11) |
| `Login.tsx` | Fixed `₹` → `??` syntax error |
| `pom.xml` | Added Flyway Maven plugin config with cleanDisabled=false |

## Run Commands for Evaluation

```powershell
# 1. Reset database
cd C:\xampp\htdocs\Projects\College\restru\backend
& "C:\Users\abhiv\tools\apache-maven-3.9.15\bin\mvn.cmd" flyway:clean flyway:migrate

# 2. Start backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:DB_HOST="localhost"; $env:DB_PORT="3306"; $env:DB_NAME="restru_db"
$env:DB_USERNAME="root"; $env:DB_PASSWORD=""
$env:JWT_SECRET="dev-secret-key-at-least-32-characters-long"
$env:SERVER_PORT="8080"
& "C:\Users\abhiv\tools\apache-maven-3.9.15\bin\mvn.cmd" spring-boot:run

# 3. Start frontend (separate terminal)
cd C:\xampp\htdocs\Projects\College\restru\frontend\frontend
npm run dev

# 4. Open http://localhost:5173
# 5. Login: customer@restru.local / admin123
# 6. Browse menu → Add to cart → Checkout → View order
```