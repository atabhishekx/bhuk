package com.restru.order.dto;

import java.time.Instant;

/**
 * Operations report for admin/dashboard.
 */
public class OperationsReportResponse {

    private Long outletId;
    private String outletName;
    private Integer totalOrders;
    private Integer ordersByStatusPlaced;
    private Integer ordersByStatusAccepted;
    private Integer ordersByStatusPreparing;
    private Integer ordersByStatusReadyForPickup;
    private Integer ordersByStatusOutForDelivery;
    private Integer ordersByStatusDelivered;
    private Integer ordersByStatusCancelled;
    private Integer ordersByStatusRejected;
    private Integer grandTotalRevenueMinor;
    private Instant generatedAt;

    public OperationsReportResponse() {
    }

    public OperationsReportResponse(Long outletId, String outletName, Integer totalOrders,
                                    Integer ordersByStatusPlaced, Integer ordersByStatusAccepted,
                                    Integer ordersByStatusPreparing, Integer ordersByStatusReadyForPickup,
                                    Integer ordersByStatusOutForDelivery, Integer ordersByStatusDelivered,
                                    Integer ordersByStatusCancelled, Integer ordersByStatusRejected,
                                    Integer grandTotalRevenueMinor, Instant generatedAt) {
        this.outletId = outletId;
        this.outletName = outletName;
        this.totalOrders = totalOrders;
        this.ordersByStatusPlaced = ordersByStatusPlaced;
        this.ordersByStatusAccepted = ordersByStatusAccepted;
        this.ordersByStatusPreparing = ordersByStatusPreparing;
        this.ordersByStatusReadyForPickup = ordersByStatusReadyForPickup;
        this.ordersByStatusOutForDelivery = ordersByStatusOutForDelivery;
        this.ordersByStatusDelivered = ordersByStatusDelivered;
        this.ordersByStatusCancelled = ordersByStatusCancelled;
        this.ordersByStatusRejected = ordersByStatusRejected;
        this.grandTotalRevenueMinor = grandTotalRevenueMinor;
        this.generatedAt = generatedAt;
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

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getOrdersByStatusPlaced() {
        return ordersByStatusPlaced;
    }

    public void setOrdersByStatusPlaced(Integer ordersByStatusPlaced) {
        this.ordersByStatusPlaced = ordersByStatusPlaced;
    }

    public Integer getOrdersByStatusAccepted() {
        return ordersByStatusAccepted;
    }

    public void setOrdersByStatusAccepted(Integer ordersByStatusAccepted) {
        this.ordersByStatusAccepted = ordersByStatusAccepted;
    }

    public Integer getOrdersByStatusPreparing() {
        return ordersByStatusPreparing;
    }

    public void setOrdersByStatusPreparing(Integer ordersByStatusPreparing) {
        this.ordersByStatusPreparing = ordersByStatusPreparing;
    }

    public Integer getOrdersByStatusReadyForPickup() {
        return ordersByStatusReadyForPickup;
    }

    public void setOrdersByStatusReadyForPickup(Integer ordersByStatusReadyForPickup) {
        this.ordersByStatusReadyForPickup = ordersByStatusReadyForPickup;
    }

    public Integer getOrdersByStatusOutForDelivery() {
        return ordersByStatusOutForDelivery;
    }

    public void setOrdersByStatusOutForDelivery(Integer ordersByStatusOutForDelivery) {
        this.ordersByStatusOutForDelivery = ordersByStatusOutForDelivery;
    }

    public Integer getOrdersByStatusDelivered() {
        return ordersByStatusDelivered;
    }

    public void setOrdersByStatusDelivered(Integer ordersByStatusDelivered) {
        this.ordersByStatusDelivered = ordersByStatusDelivered;
    }

    public Integer getOrdersByStatusCancelled() {
        return ordersByStatusCancelled;
    }

    public void setOrdersByStatusCancelled(Integer ordersByStatusCancelled) {
        this.ordersByStatusCancelled = ordersByStatusCancelled;
    }

    public Integer getOrdersByStatusRejected() {
        return ordersByStatusRejected;
    }

    public void setOrdersByStatusRejected(Integer ordersByStatusRejected) {
        this.ordersByStatusRejected = ordersByStatusRejected;
    }

    public Integer getGrandTotalRevenueMinor() {
        return grandTotalRevenueMinor;
    }

    public void setGrandTotalRevenueMinor(Integer grandTotalRevenueMinor) {
        this.grandTotalRevenueMinor = grandTotalRevenueMinor;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
