package com.restru.order.dto;

/**
 * Snapshot of a modifier on an order line item.
 */
public class OrderItemModifierResponse {

    private Long id;
    private String nameSnapshot;
    private Integer priceDeltaMinor;
    private Integer quantity;

    public OrderItemModifierResponse() {
    }

    public OrderItemModifierResponse(Long id, String nameSnapshot, Integer priceDeltaMinor, Integer quantity) {
        this.id = id;
        this.nameSnapshot = nameSnapshot;
        this.priceDeltaMinor = priceDeltaMinor;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameSnapshot() {
        return nameSnapshot;
    }

    public void setNameSnapshot(String nameSnapshot) {
        this.nameSnapshot = nameSnapshot;
    }

    public Integer getPriceDeltaMinor() {
        return priceDeltaMinor;
    }

    public void setPriceDeltaMinor(Integer priceDeltaMinor) {
        this.priceDeltaMinor = priceDeltaMinor;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
