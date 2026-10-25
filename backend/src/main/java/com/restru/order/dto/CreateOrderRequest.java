package com.restru.order.dto;

import com.restru.cart.dto.CartItemModifierResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request to create an order.
 */
public class CreateOrderRequest {

    @NotBlank
    @Size(max = 128)
    private String idempotencyKey;

    @NotNull
    private DeliveryAddress deliveryAddress;

    private String deliveryAddressJson;

    private List<LineItem> lineItems = List.of();

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(DeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryAddressJson() {
        return deliveryAddressJson;
    }

    public void setDeliveryAddressJson(String deliveryAddressJson) {
        this.deliveryAddressJson = deliveryAddressJson;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public static class DeliveryAddress {
        @NotBlank
        @Size(max = 500)
        private String fullAddress;

        @Size(max = 100)
        private String city;

        @Size(max = 100)
        private String state;

        @Size(max = 20)
        private String pincode;

        @Size(max = 100)
        private String landmark;

        @Size(max = 20)
        private String phone;

        public String getFullAddress() {
            return fullAddress;
        }

        public void setFullAddress(String fullAddress) {
            this.fullAddress = fullAddress;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPincode() {
            return pincode;
        }

        public void setPincode(String pincode) {
            this.pincode = pincode;
        }

        public String getLandmark() {
            return landmark;
        }

        public void setLandmark(String landmark) {
            this.landmark = landmark;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    public static class LineItem {
        private Long itemId;
        private Integer quantity = 1;
        private String instructions;
        private List<CartItemModifierResponse> modifiers = List.of();

        public LineItem() {
        }

        public LineItem(Long itemId, Integer quantity, String instructions, List<CartItemModifierResponse> modifiers) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.instructions = instructions;
            this.modifiers = modifiers;
        }

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

    public List<CartItemModifierResponse> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<CartItemModifierResponse> modifiers) {
        this.modifiers = modifiers;
        }
    }
}
