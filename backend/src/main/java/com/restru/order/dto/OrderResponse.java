package com.restru.order.dto;

import java.time.Instant;
import java.util.List;

/**
 * Order response with full detail including items, status history, and payment state.
 */
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerEmail;
    private Long outletId;
    private String outletName;
    private String status;
    private Integer subtotalMinor;
    private Integer taxMinor;
    private Integer deliveryFeeMinor;
    private Integer grandTotalMinor;
    private String deliveryAddressJson;
    private String idempotencyKey;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponse> items;
    private List<OrderStatusEventResponse> statusEvents;

    public OrderResponse() {
    }

    public OrderResponse(Long id, String orderNumber, Long customerId, String customerEmail, Long outletId,
                         String outletName, String status, Integer subtotalMinor, Integer taxMinor, Integer deliveryFeeMinor,
                         Integer grandTotalMinor, String deliveryAddressJson, String idempotencyKey,
                         Instant createdAt, Instant updatedAt, List<OrderItemResponse> items,
                         List<OrderStatusEventResponse> statusEvents) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.outletId = outletId;
        this.outletName = outletName;
        this.status = status;
        this.subtotalMinor = subtotalMinor;
        this.taxMinor = taxMinor;
        this.deliveryFeeMinor = deliveryFeeMinor;
        this.grandTotalMinor = grandTotalMinor;
        this.deliveryAddressJson = deliveryAddressJson;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
        this.statusEvents = statusEvents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Long getOutletId() {
        return outletId;
    }

    public void setOutletId(Long outletId) {
        this.outletId = outletId;
    }

    public String getOutletName() {
        return outletName;
    }

    public void setOutletName(String outletName) {
        this.outletName = outletName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSubtotalMinor() {
        return subtotalMinor;
    }

    public void setSubtotalMinor(Integer subtotalMinor) {
        this.subtotalMinor = subtotalMinor;
    }

    public Integer getTaxMinor() {
        return taxMinor;
    }

    public void setTaxMinor(Integer taxMinor) {
        this.taxMinor = taxMinor;
    }

    public Integer getDeliveryFeeMinor() {
        return deliveryFeeMinor;
    }

    public void setDeliveryFeeMinor(Integer deliveryFeeMinor) {
        this.deliveryFeeMinor = deliveryFeeMinor;
    }

    public Integer getGrandTotalMinor() {
        return grandTotalMinor;
    }

    public void setGrandTotalMinor(Integer grandTotalMinor) {
        this.grandTotalMinor = grandTotalMinor;
    }

    public String getDeliveryAddressJson() {
        return deliveryAddressJson;
    }

    public void setDeliveryAddressJson(String deliveryAddressJson) {
        this.deliveryAddressJson = deliveryAddressJson;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }

    public List<OrderStatusEventResponse> getStatusEvents() {
        return statusEvents;
    }

    public void setStatusEvents(List<OrderStatusEventResponse> statusEvents) {
        this.statusEvents = statusEvents;
    }
}
