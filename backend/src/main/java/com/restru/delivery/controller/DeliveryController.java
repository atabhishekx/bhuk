package com.restru.delivery.controller;

import com.restru.common.dto.ApiResponse;
import com.restru.delivery.dto.*;
import com.restru.delivery.service.DeliveryService;
import com.restru.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Delivery partner endpoints.
 * PATCH   /partner/availability          - set online/offline
 * GET     /partner/assignments           - list offers and active assignments
 * POST    /partner/assignments/{id}/accept  - accept assignment
 * POST    /partner/assignments/{id}/decline - decline with reason
 * POST    /partner/deliveries/{id}/arrived  - mark arrival at outlet
 * POST    /partner/deliveries/{id}/pickup   - confirm pickup
 * POST    /partner/deliveries/{id}/complete - mark delivery complete
 * POST    /partner/deliveries/{id}/issue    - report failed delivery/issue
 * POST    /partner/deliveries/{id}/location - update tracking location
 * GET     /partner/deliveries/{id}/tracking - get tracking history
 * GET     /track/orders/{orderId}          - customer order tracking
 * POST    /admin/deliveries/{orderId}/assign - admin manual assign/reassign
 */
@RestController
@RequestMapping("/api/v1")
public class DeliveryController {

    private final DeliveryService deliveryService;
    public DeliveryController(DeliveryService deliveryService, OrderService orderService) {
        this.deliveryService = deliveryService;
    }

    /* ---------- Partner: availability ---------- */

    @PatchMapping("/partner/availability")
    @PreAuthorize("hasRole('DELIVERY_GUY')")
    public ResponseEntity<ApiResponse<DeliveryPartnerResponse>> setAvailability(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody SetAvailabilityRequest request) {

        DeliveryPartnerResponse response = deliveryService.setAvailability(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Availability updated."));
    }

    /* ---------- Partner: assignments ---------- */

    @GetMapping("/partner/assignments")
    @PreAuthorize("hasRole('DELIVERY_GUY')")
    public ResponseEntity<ApiResponse<List<DeliveryAssignmentResponse>>> listAssignments(
            @AuthenticationPrincipal UserDetails principal) {

        List<DeliveryAssignmentResponse> assignments =
                deliveryService.listPartnerAssignments(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @PostMapping("/partner/assignments/{assignmentId}/accept")
    @PreAuthorize("hasRole('DELIVERY_GUY')")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> acceptAssignment(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long assignmentId) {

        DeliveryAssignmentResponse response =
                deliveryService.acceptAssignment(principal.getUsername(), assignmentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Assignment accepted."));
    }

    @PostMapping("/partner/assignments/{assignmentId}/decline")
    @PreAuthorize("hasRole('DELIVERY_GUY')")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> declineAssignment(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long assignmentId,
            @RequestParam String reason) {

        DeliveryAssignmentResponse response =
                deliveryService.declineAssignment(principal.getUsername(), assignmentId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Assignment declined."));
    }

    /* ---------- Partner: delivery lifecycle ---------- */

    @PostMapping("/partner/deliveries/{deliveryId}/arrived")
    @PreAuthorize("hasRole('DELIVERY_GUY')")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> markArrived(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long deliveryId) {

        DeliveryAssignmentResponse response =
                deliveryService.markArrived(principal.getUsername(), deliveryId);
        return ResponseEntity.ok(ApiResponse.success(response, "Arrived at outlet."));
    }

    @PostMapping("/partner/deliveries/{deliveryId}/pickup")
    @PreAuthorize("hasRole('DELIVERY_GUY')")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> markPickedUp(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long deliveryId) {

        DeliveryAssignmentResponse response =
                deliveryService.markPickedUp(principal.getUsername(), deliveryId);
        return ResponseEntity.ok(ApiResponse.success(response, "Order picked up."));
    }

    @PostMapping("/partner/deliveries/{deliveryId}/complete")
    @PreAuthorize("hasRole('DELIVERY_GUY')")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> markCompleted(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long deliveryId) {

        DeliveryAssignmentResponse response =
                deliveryService.markCompleted(principal.getUsername(), deliveryId);
        return ResponseEntity.ok(ApiResponse.success(response, "Delivery completed."));
    }

    @PostMapping("/partner/deliveries/{deliveryId}/issue")
    @PreAuthorize("hasRole('DELIVERY_GUY')")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> reportIssue(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long deliveryId,
            @Valid @RequestBody ReportIssueRequest request) {

        DeliveryAssignmentResponse response =
                deliveryService.reportIssue(principal.getUsername(), deliveryId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Issue reported."));
    }

    /* ---------- Partner: tracking ---------- */

    @PostMapping("/partner/deliveries/{deliveryId}/location")
    @PreAuthorize("hasRole('DELIVERY_GUY')")
    public ResponseEntity<ApiResponse<DeliveryTrackingResponse>> updateLocation(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long deliveryId,
            @Valid @RequestBody UpdateLocationRequest request) {

        DeliveryTrackingResponse response =
                deliveryService.updateLocation(principal.getUsername(), deliveryId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Location updated."));
    }

    @GetMapping("/partner/deliveries/{deliveryId}/tracking")
    @PreAuthorize("hasRole('DELIVERY_GUY')")
    public ResponseEntity<ApiResponse<List<DeliveryTrackingResponse>>> getTrackingHistory(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long deliveryId) {

        List<DeliveryTrackingResponse> history = deliveryService.getTrackingHistory(deliveryId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /* ---------- Customer: order tracking ---------- */

    @GetMapping("/track/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> trackOrder(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long orderId) {

        OrderTrackingResponse response = deliveryService.trackOrder(principal.getUsername(), orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /* ---------- Admin: available delivery partners ---------- */

    @GetMapping("/admin/delivery-partners/available")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DeliveryPartnerResponse>>> listAvailablePartners(
            @AuthenticationPrincipal UserDetails principal) {

        List<DeliveryPartnerResponse> partners = deliveryService.listAvailablePartners();
        return ResponseEntity.ok(ApiResponse.success(partners, "Available delivery partners."));
    }

    /* ---------- Admin: manual assign/reassign ---------- */

    @PostMapping("/admin/deliveries/{orderId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryAssignmentResponse>> adminAssignPartner(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long orderId,
            @RequestParam Long partnerId,
            @RequestParam String reason) {

        DeliveryAssignmentResponse response =
                deliveryService.adminAssignPartner(principal.getUsername(), orderId, partnerId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Partner assigned."));
    }
}
