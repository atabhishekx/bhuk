package com.restru.menu.dto;

/**
 * Response for a modifier option.
 */
public class ModifierOptionResponse {

    private Long id;
    private String name;
    private Integer priceDeltaMinor;
    private Boolean active;

    public ModifierOptionResponse() {
    }

    public ModifierOptionResponse(Long id, String name, Integer priceDeltaMinor, Boolean active) {
        this.id = id;
        this.name = name;
        this.priceDeltaMinor = priceDeltaMinor;
        this.active = active;
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
