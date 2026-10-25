package com.restru.order.service;

import com.restru.auth.entity.User;
import com.restru.auth.entity.UserRole;
import com.restru.auth.entity.Outlet;
import com.restru.auth.repository.OutletRepository;
import com.restru.auth.repository.UserRepository;
import com.restru.cart.dto.CartItemModifierResponse;
import com.restru.cart.dto.CartItemResponse;
import com.restru.cart.dto.CartResponse;
import com.restru.cart.service.CartService;
import com.restru.menu.entity.MenuItem;
import com.restru.menu.repository.MenuItemRepository;
import com.restru.order.dto.*;
import com.restru.order.entity.Order;
import com.restru.order.entity.OrderItem;
import com.restru.order.entity.OrderItemModifier;
import com.restru.order.entity.OrderStatusEvent;
import com.restru.order.entity.IdempotencyRecord;
import com.restru.order.repository.OrderRepository;
import com.restru.order.repository.OrderItemRepository;
import com.restru.order.repository.OrderItemModifierRepository;
import com.restru.order.repository.OrderStatusEventRepository;
import com.restru.order.repository.IdempotencyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;

/**
 * Order business logic: idempotent order creation, state-machine transitions,
 * cancellation, and payment-intent initiation.
 * Server is authoritative for all prices; cart is only a suggestion.
 */
@Service
public class OrderService {

    /* ---------- State machine ---------- */

    private static final Set<String> TERMINAL_STATES = Set.of("DELIVERED", "REJECTED", "CANCELLED", "DELIVERY_FAILED");

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "PLACED", Set.of("ACCEPTED", "REJECTED", "CANCELLED"),
            "ACCEPTED", Set.of("PREPARING", "REJECTED", "CANCELLED"),
            "PREPARING", Set.of("READY_FOR_PICKUP", "CANCELLED"),
            "READY_FOR_PICKUP", Set.of("PICKED_UP", "OUT_FOR_DELIVERY", "CANCELLED"),
            "PICKED_UP", Set.of("OUT_FOR_DELIVERY"),
            "OUT_FOR_DELIVERY", Set.of("DELIVERED", "DELIVERY_FAILED", "CANCELLED")
    );

    /* ---------- Dependencies ---------- */

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemModifierRepository orderItemModifierRepository;
    private final OrderStatusEventRepository statusEventRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final UserRepository userRepository;
    private final OutletRepository outletRepository;
    private final MenuItemRepository menuItemRepository;
    private final CartService cartService;
    private final OrderNumberGenerator orderNumberGenerator;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderItemModifierRepository orderItemModifierRepository,
                        OrderStatusEventRepository statusEventRepository,
                        IdempotencyRecordRepository idempotencyRecordRepository,
                        UserRepository userRepository,
                        OutletRepository outletRepository,
                        MenuItemRepository menuItemRepository,
                        CartService cartService,
                        OrderNumberGenerator orderNumberGenerator) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderItemModifierRepository = orderItemModifierRepository;
        this.statusEventRepository = statusEventRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.userRepository = userRepository;
        this.outletRepository = outletRepository;
        this.menuItemRepository = menuItemRepository;
        this.cartService = cartService;
        this.orderNumberGenerator = orderNumberGenerator;
    }

    /* ---------- Public read ---------- */

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String email, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!order.getCustomer().getEmail().equals(email)) {
            throw new SecurityException("Order does not belong to this customer");
        }

        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + email));

        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listStaffOrders(String email, String status) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        boolean isAdmin = user.getRoles().stream()
                .map(UserRole::getRole)
                .anyMatch("ADMIN"::equals);

        List<Order> orders;
        if (isAdmin) {
            orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            Long outletId = user.getRoles().stream()
                    .map(UserRole::getOutlet)
                    .filter(Objects::nonNull)
                    .map(com.restru.auth.entity.Outlet::getId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("User not assigned to an outlet"));

            if (status != null && !status.isBlank()) {
                orders = orderRepository.findByOutletIdAndStatusOrderByCreatedAtDesc(outletId, status);
            } else {
                orders = orderRepository.findByOutletIdOrderByCreatedAtDesc(outletId);
            }
        }

        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    /* ---------- Admin: order list (all outlets for ADMIN) ---------- */

    @Transactional(readOnly = true)
    public List<OrderResponse> listAdminOrders(String email, String status, Long outletId, String q) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        boolean isAdmin = user.getRoles().stream()
                .map(UserRole::getRole)
                .anyMatch("ADMIN"::equals);

        List<Order> orders;
        if (isAdmin) {
            orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            Long staffOutletId = user.getRoles().stream()
                    .map(UserRole::getOutlet)
                    .filter(Objects::nonNull)
                    .map(Outlet::getId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("User not assigned to an outlet"));

            if (outletId != null && !outletId.equals(staffOutletId)) {
                throw new java.lang.SecurityException("Staff cannot view orders of another outlet");
            }

            orders = orderRepository.findByOutletIdOrderByCreatedAtDesc(staffOutletId);
        }

        String query = q == null ? "" : q.trim().toLowerCase();
        String statusFilter = status == null ? "" : status.trim().toUpperCase();

        return orders.stream()
                .filter(o -> statusFilter.isEmpty() || o.getStatus().equalsIgnoreCase(statusFilter))
                .filter(o -> query.isEmpty()
                        || o.getOrderNumber().toLowerCase().contains(query)
                        || o.getCustomer().getEmail().toLowerCase().contains(query))
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + email));

        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }

        // Idempotency: return cached response if key already processed
        if (idempotencyRecordRepository.existsByRecordKey(idempotencyKey)) {
            Order existing = orderRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Idempotency record without order"));
            return toOrderResponse(existing);
        }

        // Revalidate cart server-side (client totals never trusted)
        CartResponse cart = cartService.getCart(email);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Validate each item still available and re-fetch authoritative prices
        List<CreateOrderRequest.LineItem> validatedLines = new ArrayList<>();
        for (CartItemResponse ci : cart.getItems()) {
            MenuItem item = menuItemRepository.findById(ci.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Item no longer exists: " + ci.getItemId()));
            if (!item.getActive() || !item.getAvailable()) {
                throw new IllegalArgumentException("Item no longer available: " + item.getName());
            }
            validatedLines.add(new CreateOrderRequest.LineItem(
                    ci.getItemId(),
                    ci.getQuantity(),
                    ci.getInstructions(),
                    ci.getModifiers() != null ? ci.getModifiers() : List.of()
            ));
        }

        // Compute authoritative totals from DB (never trust client)
        PricingResult pricing = computeOrderTotal(validatedLines);

        // Resolve outlet
        com.restru.auth.entity.Outlet outlet = cart.getOutletId() != null
                ? outletRepository.findById(cart.getOutletId()).orElse(null)
                : null;
        if (outlet == null) {
            throw new IllegalArgumentException("Cart has no outlet");
        }

        // Create order
        Order order = new Order();
        order.setOrderNumber(orderNumberGenerator.generate());
        order.setCustomer(user);
        order.setOutlet(outlet);
        order.setStatus("PLACED");
        order.setSubtotalMinor(pricing.subtotalMinor);
        order.setTaxMinor(pricing.taxMinor);
        order.setDeliveryFeeMinor(pricing.deliveryFeeMinor);
        order.setGrandTotalMinor(pricing.grandTotalMinor);
        order.setDeliveryAddressJson(request.getDeliveryAddressJson() != null ? request.getDeliveryAddressJson() : "{}");
        order.setIdempotencyKey(idempotencyKey);
        order = orderRepository.save(order);

        // Create order items + modifier snapshots
        for (CreateOrderRequest.LineItem line : validatedLines) {
            MenuItem item = menuItemRepository.findById(line.getItemId()).orElseThrow();
            Integer unitPrice = item.getBasePriceMinor();
            Integer lineTotal = unitPrice * line.getQuantity();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setItemNameSnapshot(item.getName());
            orderItem.setQuantity(line.getQuantity());
            orderItem.setUnitPriceMinor(unitPrice);
            orderItem.setLineTotalMinor(lineTotal);
            orderItem.setInstructions(line.getInstructions());
            orderItem = orderItemRepository.save(orderItem);

            for (CartItemModifierResponse mod : line.getModifiers()) {
                OrderItemModifier oim = new OrderItemModifier();
                oim.setOrderItem(orderItem);
                oim.setNameSnapshot(mod.getModifierName());
                oim.setPriceDeltaMinor(mod.getPriceDeltaMinor());
                oim.setQuantity(mod.getQuantity());
                orderItemModifierRepository.save(oim);
            }
        }

        // Record idempotency + status event
        IdempotencyRecord idem = new IdempotencyRecord();
        idem.setRecordKey(idempotencyKey);
        idem.setActor(user);
        idem.setOperation("CREATE_ORDER");
        idem.setRequestHash(sha256(idempotencyKey + ":" + email));
        idem.setResponseStatus(201);
        idem.setResponseBody("{\"order_number\":\"" + order.getOrderNumber() + "\",\"order_id\":" + order.getId() + "}");
        idem.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        idempotencyRecordRepository.save(idem);

        recordStatusEvent(order, null, "PLACED", user, "SYSTEM", "Order placed");

        return toOrderResponse(order);
    }

    /* ---------- Cancellation ---------- */

    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!order.getCustomer().getEmail().equals(email)) {
            throw new SecurityException("Order does not belong to this customer");
        }

        if (TERMINAL_STATES.contains(order.getStatus())) {
            throw new IllegalArgumentException("Order already in terminal state: " + order.getStatus());
        }

        if (!"PLACED".equals(order.getStatus()) && !"ACCEPTED".equals(order.getStatus())) {
            throw new IllegalArgumentException("Order cannot be cancelled from state: " + order.getStatus());
        }

        String oldStatus = order.getStatus();
        order.setStatus("CANCELLED");
        order = orderRepository.save(order);

        recordStatusEvent(order, oldStatus, "CANCELLED", userRepository.findByEmail(email).orElse(null), "USER", reason);
        return toOrderResponse(order);
    }

    /* ---------- Staff transitions ---------- */

    @Transactional
    public OrderResponse staffAcceptOrder(String staffEmail, Long orderId) {
        return staffTransition(staffEmail, orderId, "ACCEPTED", "Order accepted by staff");
    }

    @Transactional
    public OrderResponse staffRejectOrder(String staffEmail, Long orderId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        return staffTransition(staffEmail, orderId, "REJECTED", "Order rejected: " + reason);
    }

    @Transactional
    public OrderResponse staffAdvanceStatus(String staffEmail, Long orderId, String newStatus, String reason) {
        return staffTransition(staffEmail, orderId, newStatus, reason);
    }

    @Transactional
    public OrderResponse staffHandoff(String staffEmail, Long orderId, String reason) {
        return staffTransition(staffEmail, orderId, "OUT_FOR_DELIVERY", reason != null ? reason : "Delivery handoff recorded");
    }

    private OrderResponse staffTransition(String staffEmail, Long orderId, String newStatus, String reason) {
        User staff = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + staffEmail));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Outlet scoping: non-admin staff can only process orders for their outlet.
        // ADMIN is chain-wide and may process any order.
        boolean isAdmin = staff.getRoles().stream()
                .map(UserRole::getRole)
                .anyMatch("ADMIN"::equals);

        Long staffOutletId = isAdmin ? null : staff.getRoles().stream()
                .map(UserRole::getOutlet)
                .filter(Objects::nonNull)
                .map(com.restru.auth.entity.Outlet::getId)
                .findFirst()
                .orElse(null);

        if (!isAdmin && (staffOutletId == null || !staffOutletId.equals(order.getOutlet().getId()))) {
            throw new SecurityException("Staff not assigned to this order's outlet");
        }

        if (!isTransitionAllowed(order.getStatus(), newStatus)) {
            throw new IllegalArgumentException("Invalid transition " + order.getStatus() + " → " + newStatus);
        }

        String oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order = orderRepository.save(order);

        String staffRole = staff.getRoles().stream()
                .map(UserRole::getRole)
                .findFirst()
                .orElse("USER");

        recordStatusEvent(order, oldStatus, newStatus, staff, staffRole, reason);

        return toOrderResponse(order);
    }

    /* ---------- Helpers ---------- */

    private boolean isTransitionAllowed(String from, String to) {
        if (TERMINAL_STATES.contains(from)) {
            return false;
        }
        Set<String> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    private void recordStatusEvent(Order order, String oldStatus, String newStatus, User actor, String actorRole, String reason) {
        OrderStatusEvent event = new OrderStatusEvent();
        event.setOrder(order);
        event.setOldStatus(oldStatus);
        event.setNewStatus(newStatus);
        event.setActor(actor);
        event.setActorRole(actorRole);
        event.setReason(reason);
        statusEventRepository.save(event);
    }

    /* ---------- Admin: operations report ---------- */

    @Transactional(readOnly = true)
    public com.restru.order.dto.OperationsReportResponse operationsReport(String email, Long outletId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        com.restru.auth.entity.Outlet outlet = null;
        if (outletId != null) {
            outlet = outletRepository.findById(outletId)
                    .orElseThrow(() -> new IllegalArgumentException("Outlet not found: " + outletId));
        } else {
            outlet = user.getRoles().stream()
                    .map(UserRole::getOutlet)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No outlet specified"));
        }

        List<Order> orders = orderRepository.findByOutletIdOrderByCreatedAtDesc(outlet.getId());

        Integer totalOrders = orders.size();
        Integer placed = 0, accepted = 0, preparing = 0, readyForPickup = 0,
                outForDelivery = 0, delivered = 0, cancelled = 0, rejected = 0;
        Integer grandTotal = 0;

        for (Order o : orders) {
            switch (o.getStatus()) {
                case "PLACED" -> placed++;
                case "ACCEPTED" -> accepted++;
                case "PREPARING" -> preparing++;
                case "READY_FOR_PICKUP" -> readyForPickup++;
                case "OUT_FOR_DELIVERY" -> outForDelivery++;
                case "DELIVERED" -> {
                    delivered++;
                    grandTotal += o.getGrandTotalMinor() != null ? o.getGrandTotalMinor() : 0;
                }
                case "CANCELLED" -> cancelled++;
                case "REJECTED" -> rejected++;
            }
        }

        return new com.restru.order.dto.OperationsReportResponse(
                outlet.getId(), outlet.getName(),
                totalOrders, placed, accepted, preparing, readyForPickup,
                outForDelivery, delivered, cancelled, rejected,
                grandTotal, java.time.Instant.now()
        );
    }

    private PricingResult computeOrderTotal(List<CreateOrderRequest.LineItem> lines) {
        int subtotal = 0;
        for (CreateOrderRequest.LineItem line : lines) {
            MenuItem item = menuItemRepository.findById(line.getItemId()).orElseThrow();
            Integer unitPrice = item.getBasePriceMinor();
            int lineTotal = unitPrice * line.getQuantity();
            for (CartItemModifierResponse mod : line.getModifiers()) {
                lineTotal += (mod.getPriceDeltaMinor() != null ? mod.getPriceDeltaMinor() : 0) * mod.getQuantity();
            }
            subtotal += lineTotal;
        }
        int tax = roundTax(subtotal);
        int deliveryFee = 4000; // ₹40.00 flat
        int grandTotal = subtotal + tax + deliveryFee;
        return new PricingResult(subtotal, tax, deliveryFee, grandTotal);
    }

    private int roundTax(int subtotalMinor) {
        java.math.BigDecimal tax = java.math.BigDecimal.valueOf(subtotalMinor)
                .multiply(new java.math.BigDecimal("0.05"))
                .setScale(0, java.math.RoundingMode.HALF_UP);
        return tax.intValue();
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());

        List<OrderStatusEventResponse> events = statusEventRepository.findByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                .map(e -> new OrderStatusEventResponse(
                        e.getId(), e.getOldStatus(), e.getNewStatus(),
                        e.getActor() != null ? e.getActor().getEmail() : null,
                        e.getActorRole(), e.getReason(), e.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(), order.getOrderNumber(), order.getCustomer().getId(), order.getCustomer().getEmail(),
                order.getOutlet() != null ? order.getOutlet().getId() : null,
                order.getOutlet() != null ? order.getOutlet().getName() : null,
                order.getStatus(), order.getSubtotalMinor(), order.getTaxMinor(),
                order.getDeliveryFeeMinor(), order.getGrandTotalMinor(),
                order.getDeliveryAddressJson(), order.getIdempotencyKey(),
                order.getCreatedAt(), order.getUpdatedAt(),
                items, events
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem oi) {
        List<OrderItemModifierResponse> mods = oi.getModifiers().stream()
                .map(oim -> new OrderItemModifierResponse(
                        oim.getId(), oim.getNameSnapshot(), oim.getPriceDeltaMinor(), oim.getQuantity()
                ))
                .collect(Collectors.toList());
        return new OrderItemResponse(
                oi.getId(), oi.getItem() != null ? oi.getItem().getId() : null,
                oi.getItemNameSnapshot(), oi.getQuantity(), oi.getUnitPriceMinor(),
                oi.getLineTotalMinor(), oi.getInstructions(), mods
        );
    }

    private record PricingResult(int subtotalMinor, int taxMinor, int deliveryFeeMinor, int grandTotalMinor) {
    }
}
