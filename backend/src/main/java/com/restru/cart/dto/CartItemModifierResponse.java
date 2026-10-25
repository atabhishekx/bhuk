package com.restru.cart.dto;

/**
 * Response for a selected modifier on a cart line item.
 */
public class CartItemModifierResponse {

    private Long modifierOptionId;
    private String modifierName;
    private Integer priceDeltaMinor;
    private Integer quantity;
    private Integer lineTotalMinor;

    public CartItemModifierResponse() {
    }

    public CartItemModifierResponse(Long modifierOptionId, String modifierName,
                                    Integer priceDeltaMinor, Integer quantity, Integer lineTotalMinor) {
        this.modifierOptionId = modifierOptionId;
        this.modifierName = modifierName;
        this.priceDeltaMinor = priceDeltaMinor;
        this.quantity = quantity;
        this.lineTotalMinor = lineTotalMinor;
    }

    public Long getModifierOptionId() {
        return modifierOptionId;
    }

    public void setModifierOptionId(Long modifierOptionId) {
        this.modifierOptionId = modifierOptionId;
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
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

    public Integer getLineTotalMinor() {
        return lineTotalMinor;
    }

    public void setLineTotalMinor(Integer lineTotalMinor) {
        this.lineTotalMinor = lineTotalMinor;
    }
}
