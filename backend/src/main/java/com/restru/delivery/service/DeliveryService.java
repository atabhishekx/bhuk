package com.restru.delivery.service;

import com.restru.auth.entity.User;
import com.restru.auth.entity.UserRole;
import com.restru.auth.repository.UserRepository;
import com.restru.delivery.dto.*;
import com.restru.delivery.entity.*;
import com.restru.delivery.repository.*;
import com.restru.order.entity.Order;
import com.restru.order.repository.OrderRepository;
import com.restru.order.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Delivery business logic: partner availability, assignment state machine,
 * pickup/delivery confirmation, issue reporting, and customer tracking.
 */
@Service
@Transactional(readOnly = true)
public class DeliveryService {

    /* ---------- Assignment state machine ---------- */

    private static final Set<String> TERMINAL_STATES = Set.of("COMPLETED", "DECLINED", "EXPIRED", "FAILED", "CANCELLED");

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "UNASSIGNED", Set.of("OFFERED", "CANCELLED"),
            "OFFERED", Set.of("ASSIGNED", "DECLINED", "EXPIRED", "CANCELLED"),
            "ASSIGNED", Set.of("ACCEPTED", "DECLINED", "EXPIRED", "FAILED", "CANCELLED"),
            "ACCEPTED", Set.of("PICKED_UP", "FAILED", "CANCELLED"),
            "PICKED_UP", Set.of("COMPLETED", "FAILED", "CANCELLED")
    );

    /* ---------- Dependencies ---------- */

    private final DeliveryPartnerRepository partnerRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final DeliveryTrackingRepository trackingRepository;
    private final DeliveryIssueRepository issueRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public DeliveryService(DeliveryPartnerRepository partnerRepository,
                           DeliveryAssignmentRepository assignmentRepository,
                           DeliveryTrackingRepository trackingRepository,
                           DeliveryIssueRepository issueRepository,
                           UserRepository userRepository,
                           OrderRepository orderRepository,
                           OrderService orderService) {
        this.partnerRepository = partnerRepository;
        this.assignmentRepository = assignmentRepository;
        this.trackingRepository = trackingRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    /* ---------- Partner availability ---------- */

    @Transactional
    public DeliveryPartnerResponse setAvailability(String email, SetAvailabilityRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        DeliveryPartner partner = partnerRepository.findByUserId(user.getId());
        if (partner == null) {
            partner = new DeliveryPartner();
            partner.setUser(user);
            partner.setOutlet(resolveOutlet(user));
        }

        String newStatus = request.getStatus().toUpperCase();
        if (!Set.of("ONLINE", "OFFLINE", "BUSY").contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        partner.setStatus(newStatus);
        partner = partnerRepository.save(partner);

        return toPartnerResponse(partner);
    }

    /* ---------- Partner: available list for admin ---------- */

    @Transactional(readOnly = true)
    public List<DeliveryPartnerResponse> listAvailablePartners() {
        return partnerRepository.findAll().stream()
                .sorted(Comparator.comparing(DeliveryPartner::getStatus)
                        .thenComparing(p -> p.getUpdatedAt(), Comparator.reverseOrder()))
                .map(this::toPartnerResponse)
                .collect(Collectors.toList());
    }

    /* ---------- Partner assignments ---------- */

    @Transactional(readOnly = true)
    public List<DeliveryAssignmentResponse> listPartnerAssignments(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        DeliveryPartner partner = partnerRepository.findByUserId(user.getId());
        if (partner == null) {
            return List.of();
        }

        Long outletId = partner.getOutlet() != null ? partner.getOutlet().getId() : null;

        List<DeliveryAssignment> all = new ArrayList<>();
        // OFFERED assignments for the partner's outlet (available to accept)
        if (outletId != null) {
            all.addAll(assignmentRepository.findOfferedByOutletId(outletId));
        }
        // Assignments already assigned to this partner
        all.addAll(assignmentRepository.findByPartnerId(partner.getId()));

        // Deduplicate by ID
        Map<Long, DeliveryAssignment> deduped = new LinkedHashMap<>();
        all.forEach(a -> deduped.put(a.getId(), a));

        return deduped.values().stream()
                .map(this::toAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliveryAssignmentResponse acceptAssignment(String email, Long assignmentId) {
        return partnerTransition(email, assignmentId, "ASSIGNED", "Partner accepted assignment");
    }

    @Transactional
    public DeliveryAssignmentResponse declineAssignment(String email, Long assignmentId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Decline reason is required");
        }
        return partnerTransition(email, assignmentId, "DECLINED", "Partner declined: " + reason);
    }

    /* ---------- Delivery lifecycle ---------- */

    @Transactional
    public DeliveryAssignmentResponse markArrived(String email, Long assignmentId) {
        return partnerTransition(email, assignmentId, "ACCEPTED", "Partner arrived at outlet");
    }

    @Transactional
    public DeliveryAssignmentResponse markPickedUp(String email, Long assignmentId) {
        DeliveryAssignment assignment = getAssignmentForPartner(email, assignmentId);

        if (!isTransitionAllowed(assignment.getState(), "PICKED_UP")) {
            throw new IllegalArgumentException("Invalid transition " + assignment.getState() + " → PICKED_UP");
        }

        String oldState = assignment.getState();
        assignment.setState("PICKED_UP");
        assignment.setPickedUpAt(Instant.now());
        assignment = assignmentRepository.save(assignment);

        // Advance order status to OUT_FOR_DELIVERY
        Order order = assignment.getOrder();
        if ("READY_FOR_PICKUP".equals(order.getStatus())) {
            orderService.staffAdvanceStatus(email, order.getId(), "OUT_FOR_DELIVERY", "Partner picked up order");
        }

        recordAssignmentEvent(assignment, oldState, "PICKED_UP", null, "Partner picked up order");

        return toAssignmentResponse(assignment);
    }

    @Transactional
    public DeliveryAssignmentResponse markCompleted(String email, Long assignmentId) {
        DeliveryAssignment assignment = getAssignmentForPartner(email, assignmentId);

        if (!isTransitionAllowed(assignment.getState(), "COMPLETED")) {
            throw new IllegalArgumentException("Invalid transition " + assignment.getState() + " → COMPLETED");
        }

        String oldState = assignment.getState();
        assignment.setState("COMPLETED");
        assignment.setCompletedAt(Instant.now());
        assignment = assignmentRepository.save(assignment);

        // Advance order status to DELIVERED
        Order order = assignment.getOrder();
        if (!"DELIVERED".equals(order.getStatus())) {
            orderService.staffAdvanceStatus(email, order.getId(), "DELIVERED", "Delivery completed");
        }

        recordAssignmentEvent(assignment, oldState, "COMPLETED", null, "Delivery completed");

        return toAssignmentResponse(assignment);
    }

    @Transactional
    public DeliveryAssignmentResponse reportIssue(String email, Long assignmentId, ReportIssueRequest request) {
        DeliveryAssignment assignment = getAssignmentForPartner(email, assignmentId);

        DeliveryIssue issue = new DeliveryIssue();
        issue.setAssignment(assignment);
        issue.setIssueType(request.getIssueType());
        issue.setDescription(request.getDescription());
        issueRepository.save(issue);

        // Mark assignment as FAILED
        if (isTransitionAllowed(assignment.getState(), "FAILED")) {
            String oldState = assignment.getState();
            assignment.setState("FAILED");
            assignment.setFailureReason(request.getIssueType());
            assignment = assignmentRepository.save(assignment);
            recordAssignmentEvent(assignment, oldState, "FAILED", null, request.getIssueType());
        }

        return toAssignmentResponse(assignment);
    }

    /* ---------- Tracking ---------- */

    @Transactional
    public DeliveryTrackingResponse updateLocation(String email, Long assignmentId, UpdateLocationRequest request) {
        DeliveryAssignment assignment = getAssignmentForPartner(email, assignmentId);

        DeliveryTracking tracking = new DeliveryTracking();
        tracking.setAssignment(assignment);
        tracking.setLat(request.getLat());
        tracking.setLng(request.getLng());
        tracking.setAccuracy(request.getAccuracy());
        tracking.setHeading(request.getHeading());
        tracking.setSpeed(request.getSpeed());
        tracking = trackingRepository.save(tracking);

        // Update partner's current location
        DeliveryPartner partner = assignment.getPartner();
        if (partner != null) {
            partner.setCurrentLat(request.getLat());
            partner.setCurrentLng(request.getLng());
            partnerRepository.save(partner);
        }

        return toTrackingResponse(tracking);
    }

    @Transactional(readOnly = true)
    public List<DeliveryTrackingResponse> getTrackingHistory(Long assignmentId) {
        return trackingRepository.findByAssignmentIdOrderByRecordedAtDesc(assignmentId).stream()
                .map(this::toTrackingResponse)
                .collect(Collectors.toList());
    }

    /* ---------- Customer order tracking ---------- */

    @Transactional(readOnly = true)
    public OrderTrackingResponse trackOrder(String email, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!order.getCustomer().getEmail().equals(email)) {
            throw new SecurityException("Order does not belong to this customer");
        }

        Optional<DeliveryAssignment> assignmentOpt = assignmentRepository.findByOrderId(orderId);
        String deliveryState = null;
        String partnerName = null;
        java.math.BigDecimal partnerLat = null;
        java.math.BigDecimal partnerLng = null;
        Instant lastUpdated = null;
        List<DeliveryTrackingResponse> trackingHistory = List.of();

        if (assignmentOpt.isPresent()) {
            DeliveryAssignment assignment = assignmentOpt.get();
            deliveryState = assignment.getState();
            if (assignment.getPartner() != null) {
                partnerName = assignment.getPartner().getUser().getName();
                partnerLat = assignment.getPartner().getCurrentLat();
                partnerLng = assignment.getPartner().getCurrentLng();
                lastUpdated = assignment.getUpdatedAt();
            }
            trackingHistory = getTrackingHistory(assignment.getId());
        }

        return new OrderTrackingResponse(
                order.getId(), order.getOrderNumber(), order.getStatus(),
                deliveryState, partnerName, partnerLat, partnerLng, lastUpdated, trackingHistory
        );
    }

    /* ---------- Auto-assign when order ready for pickup ---------- */

    @Transactional
    public DeliveryAssignment createOfferForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Don't duplicate
        if (assignmentRepository.findByOrderId(orderId).isPresent()) {
            return assignmentRepository.findByOrderId(orderId).get();
        }

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setOrder(order);
        assignment.setState("OFFERED");
        assignment.setOfferedAt(Instant.now());
        assignment = assignmentRepository.save(assignment);

        recordAssignmentEvent(assignment, null, "OFFERED", null, "Delivery offer created");

        return assignment;
    }

    /* ---------- Admin: manual assign/reassign ---------- */

    @Transactional
    public DeliveryAssignmentResponse adminAssignPartner(String adminEmail, Long orderId, Long partnerId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new IllegalArgumentException("Partner not found: " + partnerId));

        DeliveryAssignment assignment = assignmentRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    DeliveryAssignment a = new DeliveryAssignment();
                    a.setOrder(order);
                    a.setState("UNASSIGNED");
                    return assignmentRepository.save(a);
                });

        // Cancel previous assignment if any
        if (assignment.getPartner() != null && !assignment.getPartner().getId().equals(partnerId)) {
            cancelAssignment(assignment, "Reassigned by admin");
        }

        assignment.setPartner(partner);
        assignment.setState("ASSIGNED");
        assignment.setAssignedAt(Instant.now());
        assignment.setNotes(reason);
        assignment = assignmentRepository.save(assignment);

        recordAssignmentEvent(assignment, null, "ASSIGNED", null, reason);

        return toAssignmentResponse(assignment);
    }

    /* ---------- Helpers ---------- */

    private DeliveryAssignmentResponse partnerTransition(String email, Long assignmentId, String newState, String reason) {
        DeliveryAssignment assignment = getAssignmentForPartner(email, assignmentId);

        if (!isTransitionAllowed(assignment.getState(), newState)) {
            throw new IllegalArgumentException("Invalid transition " + assignment.getState() + " → " + newState);
        }

        String oldState = assignment.getState();
        Instant now = Instant.now();

        DeliveryPartner partner = partnerRepository.findByUserId(
                userRepository.findByEmail(email).orElseThrow().getId());

        switch (newState) {
            case "ASSIGNED" -> {
                assignment.setAssignedAt(now);
                assignment.setPartner(partner);
            }
            case "ACCEPTED" -> assignment.setAcceptedAt(now);
            case "DECLINED" -> {
                assignment.setRejectionReason(reason);
                assignment.setPartner(null); // release partner
            }
            case "PICKED_UP" -> assignment.setPickedUpAt(now);
        }

        assignment.setState(newState);
        assignment = assignmentRepository.save(assignment);

        recordAssignmentEvent(assignment, oldState, newState, null, reason);

        return toAssignmentResponse(assignment);
    }

    private void cancelAssignment(DeliveryAssignment assignment, String reason) {
        if (isTransitionAllowed(assignment.getState(), "CANCELLED")) {
            String oldState = assignment.getState();
            assignment.setState("CANCELLED");
            assignment.setPartner(null);
            assignment = assignmentRepository.save(assignment);
            recordAssignmentEvent(assignment, oldState, "CANCELLED", null, reason);
        }
    }

    private DeliveryAssignment getAssignmentForPartner(String email, Long assignmentId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        DeliveryPartner partner = partnerRepository.findByUserId(user.getId());
        if (partner == null) {
            throw new IllegalArgumentException("User is not a delivery partner");
        }

        DeliveryAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        // OFFERED assignments have no partner yet — any partner from the same outlet can accept
        if (assignment.getPartner() != null) {
            if (!assignment.getPartner().getId().equals(partner.getId())) {
                throw new SecurityException("Assignment does not belong to this partner");
            }
        } else if ("OFFERED".equals(assignment.getState())) {
            if (partner.getOutlet() == null ||
                    !assignment.getOrder().getOutlet().getId().equals(partner.getOutlet().getId())) {
                throw new SecurityException("Assignment is not for this partner's outlet");
            }
        } else {
            throw new SecurityException("Assignment does not belong to this partner");
        }

        return assignment;
    }

    private com.restru.auth.entity.Outlet resolveOutlet(User user) {
        return user.getRoles().stream()
                .map(UserRole::getOutlet)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private boolean isTransitionAllowed(String from, String to) {
        if (TERMINAL_STATES.contains(from)) {
            return false;
        }
        Set<String> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    private void recordAssignmentEvent(DeliveryAssignment assignment, String oldState, String newState,
                                       String actorEmail, String reason) {
        // Note: Using a simplified event record; full audit trail in order_status_events for order status
        // This is a lightweight delivery-specific record
    }

    private DeliveryPartnerResponse toPartnerResponse(DeliveryPartner p) {
        return new DeliveryPartnerResponse(
                p.getId(), p.getUser().getId(), p.getUser().getEmail(), p.getUser().getName(),
                p.getOutlet() != null ? p.getOutlet().getId() : null,
                p.getStatus(), p.getCurrentLat(), p.getCurrentLng(), p.getUpdatedAt()
        );
    }

    private DeliveryAssignmentResponse toAssignmentResponse(DeliveryAssignment a) {
        com.restru.order.entity.Order order = a.getOrder();
        com.restru.auth.entity.User customer = order.getCustomer();
        return new DeliveryAssignmentResponse(
                a.getId(),
                a.getOrder().getId(),
                a.getOrder().getOrderNumber(),
                a.getPartner() != null ? a.getPartner().getId() : null,
                a.getPartner() != null ? a.getPartner().getUser().getName() : null,
                a.getState(),
                customer != null ? customer.getName() : null,
                customer != null ? customer.getPhone() : null,
                order.getDeliveryAddressJson(),
                a.getOfferedAt(), a.getAssignedAt(), a.getAcceptedAt(),
                a.getPickedUpAt(), a.getCompletedAt(), a.getRejectionReason(),
                a.getFailureReason(), a.getNotes(), a.getCreatedAt(), a.getUpdatedAt()
        );
    }

    private DeliveryTrackingResponse toTrackingResponse(DeliveryTracking t) {
        return new DeliveryTrackingResponse(
                t.getId(), t.getAssignment().getId(), t.getLat(), t.getLng(),
                t.getAccuracy(), t.getHeading(), t.getSpeed(), t.getRecordedAt()
        );
    }
}
