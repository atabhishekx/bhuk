package com.restru.menu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request to create or update a modifier option.
 */
public class ModifierOptionRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @Min(0)
    private Integer priceDeltaMinor = 0;

    private Boolean active = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPriceDeltaMinor() {
        return priceDeltaMinor;
    }

    public void setPriceDeltaMinor(Integer priceDeltaMinor) {
        this.priceDeltaMinor = priceDeltaMinor;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
