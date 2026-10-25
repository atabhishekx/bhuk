package com.restru.delivery.dto;

import java.time.Instant;
import java.util.List;

/**
 * Order tracking response for customers.
 */
public class OrderTrackingResponse {

    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private String deliveryState;
    private String partnerName;
    private java.math.BigDecimal partnerLat;
    private java.math.BigDecimal partnerLng;
    private Instant lastUpdated;
    private List<DeliveryTrackingResponse> trackingHistory;

    public OrderTrackingResponse() {
    }

    public OrderTrackingResponse(Long orderId, String orderNumber, String orderStatus, String deliveryState,
                                 String partnerName, java.math.BigDecimal partnerLat, java.math.BigDecimal partnerLng,
                                 Instant lastUpdated, List<DeliveryTrackingResponse> trackingHistory) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.orderStatus = orderStatus;
        this.deliveryState = deliveryState;
        this.partnerName = partnerName;
        this.partnerLat = partnerLat;
        this.partnerLng = partnerLng;
        this.lastUpdated = lastUpdated;
        this.trackingHistory = trackingHistory;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getDeliveryState() { return deliveryState; }
    public void setDeliveryState(String deliveryState) { this.deliveryState = deliveryState; }
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public java.math.BigDecimal getPartnerLat() { return partnerLat; }
    public void setPartnerLat(java.math.BigDecimal partnerLat) { this.partnerLat = partnerLat; }
    public java.math.BigDecimal getPartnerLng() { return partnerLng; }
    public void setPartnerLng(java.math.BigDecimal partnerLng) { this.partnerLng = partnerLng; }
    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
    public List<DeliveryTrackingResponse> getTrackingHistory() { return trackingHistory; }
    public void setTrackingHistory(List<DeliveryTrackingResponse> trackingHistory) { this.trackingHistory = trackingHistory; }
}
