package com.restru.menu.dto;

import java.util.List;

/**
 * Response for a modifier group including its options.
 */
public class ModifierGroupResponse {

    private Long id;
    private String name;
    private Integer minSelect;
    private Integer maxSelect;
    private Boolean required;
    private Boolean active;
    private List<ModifierOptionResponse> options;

    public ModifierGroupResponse() {
    }

    public ModifierGroupResponse(Long id, String name, Integer minSelect, Integer maxSelect,
                                 Boolean required, Boolean active, List<ModifierOptionResponse> options) {
        this.id = id;
        this.name = name;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.required = required;
        this.active = active;
        this.options = options;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<ModifierOptionResponse> getOptions() {
        return options;
    }

    public void setOptions(List<ModifierOptionResponse> options) {
        this.options = options;
    }
}
