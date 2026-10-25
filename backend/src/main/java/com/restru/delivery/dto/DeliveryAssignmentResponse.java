package com.restru.delivery.dto;

import java.time.Instant;

/**
 * Delivery assignment response.
 */
public class DeliveryAssignmentResponse {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long partnerId;
    private String partnerName;
    private String state;
    private String customerName;
    private String customerPhone;
    private String deliveryAddressJson;
    private Instant offeredAt;
    private Instant assignedAt;
    private Instant acceptedAt;
    private Instant pickedUpAt;
    private Instant completedAt;
    private String rejectionReason;
    private String failureReason;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public DeliveryAssignmentResponse() {
    }

    public DeliveryAssignmentResponse(Long id, Long orderId, String orderNumber, Long partnerId, String partnerName,
                                      String state, String customerName, String customerPhone, String deliveryAddressJson,
                                      Instant offeredAt, Instant assignedAt, Instant acceptedAt,
                                      Instant pickedUpAt, Instant completedAt, String rejectionReason,
                                      String failureReason, String notes, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.state = state;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.deliveryAddressJson = deliveryAddressJson;
        this.offeredAt = offeredAt;
        this.assignedAt = assignedAt;
        this.acceptedAt = acceptedAt;
        this.pickedUpAt = pickedUpAt;
        this.completedAt = completedAt;
        this.rejectionReason = rejectionReason;
        this.failureReason = failureReason;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getDeliveryAddressJson() { return deliveryAddressJson; }
    public void setDeliveryAddressJson(String deliveryAddressJson) { this.deliveryAddressJson = deliveryAddressJson; }
    public Instant getOfferedAt() { return offeredAt; }
    public void setOfferedAt(Instant offeredAt) { this.offeredAt = offeredAt; }
    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Instant acceptedAt) { this.acceptedAt = acceptedAt; }
    public Instant getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(Instant pickedUpAt) { this.pickedUpAt = pickedUpAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
