package com.restru.menu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to create or update a modifier group.
 */
public class ModifierGroupRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Min(0)
    private Integer minSelect = 0;

    @Min(0)
    private Integer maxSelect = 0;

    private Boolean required = false;

    private Boolean active = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMinSelect() {
        return minSelect;
    }

    public void setMinSelect(Integer minSelect) {
        this.minSelect = minSelect;
    }

    public Integer getMaxSelect() {
        return maxSelect;
    }

    public void setMaxSelect(Integer maxSelect) {
        this.maxSelect = maxSelect;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
