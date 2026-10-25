package com.restru.cart.dto;

import java.util.List;

/**
 * Response for a cart line item.
 */
public class CartItemResponse {

    private Long id;
    private Long itemId;
    private String itemName;
    private Integer basePriceMinor;
    private Integer quantity;
    private String instructions;
    private List<CartItemModifierResponse> modifiers;
    private Integer lineTotalMinor;

    public CartItemResponse() {
    }

    public CartItemResponse(Long id, Long itemId, String itemName, Integer basePriceMinor,
                            Integer quantity, String instructions, List<CartItemModifierResponse> modifiers,
                            Integer lineTotalMinor) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        this.basePriceMinor = basePriceMinor;
        this.quantity = quantity;
        this.instructions = instructions;
        this.modifiers = modifiers;
        this.lineTotalMinor = lineTotalMinor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getBasePriceMinor() {
        return basePriceMinor;
    }

    public void setBasePriceMinor(Integer basePriceMinor) {
        this.basePriceMinor = basePriceMinor;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<CartItemModifierResponse> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<CartItemModifierResponse> modifiers) {
        this.modifiers = modifiers;
    }

    public Integer getLineTotalMinor() {
        return lineTotalMinor;
    }

    public void setLineTotalMinor(Integer lineTotalMinor) {
        this.lineTotalMinor = lineTotalMinor;
    }
}
