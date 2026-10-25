package com.restru.order.controller;

import com.restru.common.dto.ApiResponse;
import com.restru.delivery.service.DeliveryService;
import com.restru.order.dto.*;
import com.restru.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Order endpoints.
 * Customer:
 *   POST   /api/v1/orders                 - create order (idempotent)
 *   GET    /api/v1/orders                 - list own orders
 *   GET    /api/v1/orders/{orderId}       - view own order
 *   POST   /api/v1/orders/{orderId}/cancel - cancel if eligible
 *   POST   /api/v1/orders/{orderId}/payment-intent - initiate payment
 *   GET    /api/v1/orders/{orderId}/payment - retrieve payment state
 * Staff (outlet-scoped):
 *   POST   /api/v1/staff/orders/{orderId}/accept
 *   POST   /api/v1/staff/orders/{orderId}/reject
 *   POST   /api/v1/staff/orders/{orderId}/status
 *   POST   /api/v1/staff/orders/{orderId}/handoff
 *   GET    /api/v1/staff/orders              - outlet-scoped order queue
 * Admin (all outlets):
 *   GET    /api/v1/admin/orders              - search/filter orders (status, outletId, q)
 */
@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;
    private final DeliveryService deliveryService;

    public OrderController(OrderService orderService, DeliveryService deliveryService) {
        this.orderService = orderService;
        this.deliveryService = deliveryService;
    }

    /* ---------- Customer: order creation ---------- */

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse order = orderService.createOrder(principal.getUsername(), request);
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order created."));
    }

    /* ---------- Customer: order list / detail ---------- */

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> listOrders(
            @AuthenticationPrincipal UserDetails principal) {

        List<OrderResponse> orders = orderService.listOrders(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long orderId) {

        OrderResponse order = orderService.getOrder(principal.getUsername(), orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /* ---------- Customer: cancellation ---------- */

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {

        OrderResponse order = orderService.cancelOrder(principal.getUsername(), orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(order, "Order cancelled."));
    }

    /* ---------- Staff: order queue transitions ---------- */

    @PostMapping("/staff/orders/{orderId}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> staffAccept(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long orderId) {

        OrderResponse order = orderService.staffAcceptOrder(principal.getUsername(), orderId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order accepted."));
    }

    @PostMapping("/staff/orders/{orderId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> staffReject(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long orderId,
            @RequestParam String reason) {

        OrderResponse order = orderService.staffRejectOrder(principal.getUsername(), orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(order, "Order rejected."));
    }

    @PostMapping("/staff/orders/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> staffAdvanceStatus(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long orderId,
            @RequestParam String newStatus,
            @RequestParam(required = false) String reason) {

        OrderResponse order = orderService.staffAdvanceStatus(principal.getUsername(), orderId, newStatus, reason);

        // Auto-create delivery offer when order is ready for pickup
        if ("READY_FOR_PICKUP".equals(newStatus)) {
            deliveryService.createOfferForOrder(orderId);
        }

        return ResponseEntity.ok(ApiResponse.success(order, "Order status advanced."));
    }

    @PostMapping("/staff/orders/{orderId}/handoff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> staffHandoff(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {

        OrderResponse order = orderService.staffHandoff(principal.getUsername(), orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(order, "Delivery handoff recorded."));
    }

    @GetMapping("/staff/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> staffOrderQueue(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String status) {

        List<OrderResponse> orders = orderService.listStaffOrders(principal.getUsername(), status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /* ---------- Admin: order list ---------- */

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> adminOrderList(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long outletId,
            @RequestParam(required = false) String q) {

        List<OrderResponse> orders = orderService.listAdminOrders(principal.getUsername(), status, outletId, q);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders loaded."));
    }

    /* ---------- Admin: reports ---------- */

    @GetMapping("/admin/reports/operations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.restru.order.dto.OperationsReportResponse>> operationsReport(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) Long outletId) {

        com.restru.order.dto.OperationsReportResponse report =
                orderService.operationsReport(principal.getUsername(), outletId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
