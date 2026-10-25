package com.restru.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Request to add or update an item in the cart.
 */
public class AddToCartRequest {

    @NotNull
    private Long itemId;

    @NotNull
    @Positive
    private Integer quantity = 1;

    private String instructions;

    private List<Long> modifierOptionIds = List.of();

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
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

    public List<Long> getModifierOptionIds() {
        return modifierOptionIds;
    }

    public void setModifierOptionIds(List<Long> modifierOptionIds) {
        this.modifierOptionIds = modifierOptionIds;
    }
}
