package com.restru.cart.dto;

/**
 * Checkout quote response: authoritative pricing before order placement.
 */
public class CheckoutQuoteResponse {

    private Integer subtotalMinor;
    private Integer taxMinor;
    private Integer deliveryFeeMinor;
    private Integer grandTotalMinor;
    private Integer itemCount;

    public CheckoutQuoteResponse() {
    }

    public CheckoutQuoteResponse(Integer subtotalMinor, Integer taxMinor, Integer deliveryFeeMinor,
                                 Integer grandTotalMinor, Integer itemCount) {
        this.subtotalMinor = subtotalMinor;
        this.taxMinor = taxMinor;
        this.deliveryFeeMinor = deliveryFeeMinor;
        this.grandTotalMinor = grandTotalMinor;
        this.itemCount = itemCount;
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
