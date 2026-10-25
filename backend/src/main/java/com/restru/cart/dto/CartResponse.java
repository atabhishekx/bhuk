package com.restru.cart.dto;

import java.util.List;

/**
 * Cart summary response including authoritative server-side pricing.
 */
public class CartResponse {

    private Long id;
    private Long customerId;
    private Long outletId;
    private List<CartItemResponse> items;
    private Integer subtotalMinor;
    private Integer taxMinor;
    private Integer deliveryFeeMinor;
    private Integer grandTotalMinor;
    private Integer itemCount;

    public CartResponse() {
    }

    public CartResponse(Long id, Long customerId, Long outletId, List<CartItemResponse> items,
                        Integer subtotalMinor, Integer taxMinor, Integer deliveryFeeMinor,
                        Integer grandTotalMinor, Integer itemCount) {
        this.id = id;
        this.customerId = customerId;
        this.outletId = outletId;
        this.items = items;
        this.subtotalMinor = subtotalMinor;
        this.taxMinor = taxMinor;
        this.deliveryFeeMinor = deliveryFeeMinor;
        this.grandTotalMinor = grandTotalMinor;
        this.itemCount = itemCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getOutletId() {
        return outletId;
    }

    public void setOutletId(Long outletId) {
        this.outletId = outletId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
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

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
}
