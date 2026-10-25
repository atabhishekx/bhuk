package com.restru.menu.dto;

import java.util.List;

/**
 * Response for a menu item including its modifier groups.
 */
public class ItemResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private Integer basePriceMinor;
    private String imageUrl;
    private String dietaryType;
    private Boolean active;
    private Boolean available;
    private List<ModifierGroupResponse> modifierGroups;

    public ItemResponse() {
    }

    public ItemResponse(Long id, Long categoryId, String categoryName, String name, String description,
                        Integer basePriceMinor, String imageUrl, String dietaryType,
                        Boolean active, Boolean available,
                        List<ModifierGroupResponse> modifierGroups) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.basePriceMinor = basePriceMinor;
        this.imageUrl = imageUrl;
        this.dietaryType = dietaryType;
        this.active = active;
        this.available = available;
        this.modifierGroups = modifierGroups;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getBasePriceMinor() {
        return basePriceMinor;
    }

    public void setBasePriceMinor(Integer basePriceMinor) {
        this.basePriceMinor = basePriceMinor;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDietaryType() {
        return dietaryType;
    }

    public void setDietaryType(String dietaryType) {
        this.dietaryType = dietaryType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public List<ModifierGroupResponse> getModifierGroups() {
        return modifierGroups;
    }

    public void setModifierGroups(List<ModifierGroupResponse> modifierGroups) {
        this.modifierGroups = modifierGroups;
    }
}
