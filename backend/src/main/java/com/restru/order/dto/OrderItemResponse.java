package com.restru.order.dto;

import java.util.List;

/**
 * Order line item response with snapshot data.
 */
public class OrderItemResponse {

    private Long id;
    private Long itemId;
    private String itemNameSnapshot;
    private Integer quantity;
    private Integer unitPriceMinor;
    private Integer lineTotalMinor;
    private String instructions;
    private List<OrderItemModifierResponse> modifiers;

    public OrderItemResponse() {
    }

    public OrderItemResponse(Long id, Long itemId, String itemNameSnapshot, Integer quantity,
                             Integer unitPriceMinor, Integer lineTotalMinor, String instructions,
                             List<OrderItemModifierResponse> modifiers) {
        this.id = id;
        this.itemId = itemId;
        this.itemNameSnapshot = itemNameSnapshot;
        this.quantity = quantity;
        this.unitPriceMinor = unitPriceMinor;
        this.lineTotalMinor = lineTotalMinor;
        this.instructions = instructions;
        this.modifiers = modifiers;
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

    public String getItemNameSnapshot() {
        return itemNameSnapshot;
    }

    public void setItemNameSnapshot(String itemNameSnapshot) {
        this.itemNameSnapshot = itemNameSnapshot;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getUnitPriceMinor() {
        return unitPriceMinor;
    }

    public void setUnitPriceMinor(Integer unitPriceMinor) {
        this.unitPriceMinor = unitPriceMinor;
    }

    public Integer getLineTotalMinor() {
        return lineTotalMinor;
    }

    public void setLineTotalMinor(Integer lineTotalMinor) {
        this.lineTotalMinor = lineTotalMinor;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<OrderItemModifierResponse> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<OrderItemModifierResponse> modifiers) {
        this.modifiers = modifiers;
    }
}
