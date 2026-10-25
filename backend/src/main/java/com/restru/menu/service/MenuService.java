package com.restru.menu.service;

import com.restru.auth.repository.OutletRepository;
import com.restru.menu.dto.CategoryRequest;
import com.restru.menu.dto.CategoryResponse;
import com.restru.menu.dto.ItemRequest;
import com.restru.menu.dto.ItemResponse;
import com.restru.menu.dto.ModifierGroupRequest;
import com.restru.menu.dto.ModifierGroupResponse;
import com.restru.menu.dto.ModifierOptionRequest;
import com.restru.menu.dto.ModifierOptionResponse;
import com.restru.menu.dto.ServiceabilityResponse;
import com.restru.menu.entity.MenuCategory;
import com.restru.menu.entity.MenuItem;
import com.restru.menu.entity.ModifierGroup;
import com.restru.menu.entity.ModifierOption;
import com.restru.menu.repository.MenuCategoryRepository;
import com.restru.menu.repository.MenuItemRepository;
import com.restru.menu.repository.ModifierGroupRepository;
import com.restru.menu.repository.ModifierOptionRepository;
import com.restru.menu.repository.OutletItemAvailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Menu business logic: read (public) and write (admin) operations for categories,
 * items, modifier groups, and modifier options.
 */
@Service
@Transactional(readOnly = true)
public class MenuService {

    private final OutletRepository outletRepository;
    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final ModifierGroupRepository modifierGroupRepository;
    private final ModifierOptionRepository modifierOptionRepository;
    public MenuService(OutletRepository outletRepository,
                       MenuCategoryRepository categoryRepository,
                       MenuItemRepository itemRepository,
                       ModifierGroupRepository modifierGroupRepository,
                       ModifierOptionRepository modifierOptionRepository,
                       OutletItemAvailabilityRepository availabilityRepository) {
        this.outletRepository = outletRepository;
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.modifierGroupRepository = modifierGroupRepository;
        this.modifierOptionRepository = modifierOptionRepository;
    }

    /* ---------- Public read ---------- */

    public List<CategoryResponse> listCategories(Long outletId) {
        List<MenuCategory> categories = categoryRepository.findAll();
        return categories.stream().map(this::toCategoryResponse).collect(Collectors.toList());
    }

    public List<ItemResponse> listItems(Long outletId, Long categoryId, boolean all) {
        List<MenuItem> items;
        if (categoryId != null) {
            items = all ? itemRepository.findByCategoryId(categoryId)
                    : itemRepository.findByCategoryIdAndActiveTrueAndAvailableTrue(categoryId);
        } else {
            items = all ? itemRepository.findAll()
                    : itemRepository.findByActiveTrueAndAvailableTrue();
        }
        return items.stream().map(item -> toItemResponse(item, outletId)).collect(Collectors.toList());
    }

    public ItemResponse getItem(Long itemId, Long outletId) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemId));
        return toItemResponse(item, outletId);
    }

    public ServiceabilityResponse checkServiceability(String pincode, Long outletId) {
        com.restru.auth.entity.Outlet outlet = null;
        if (outletId != null) {
            outlet = outletRepository.findById(outletId)
                    .orElseThrow(() -> new IllegalArgumentException("Outlet not found: " + outletId));
        } else {
            // Return the first active outlet
            outlet = outletRepository.findByStatus("ACTIVE").stream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No active outlet found"));
        }

        boolean serviceable = "ACTIVE".equalsIgnoreCase(outlet.getStatus())
                && (outlet.getPincode() != null && outlet.getPincode().equals(pincode));

        return new ServiceabilityResponse(
                pincode,
                serviceable,
                outlet.getId(),
                outlet.getName(),
                serviceable ? "Address is serviceable by this outlet."
                        : "Address is outside delivery coverage for this outlet."
        );
    }

    /* ---------- Admin write ---------- */

    @Transactional
    public CategoryResponse createCategory(Long outletId, CategoryRequest request) {
        MenuCategory category = new MenuCategory();
        category.setOutlet(loadOutlet(outletId));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setActive(request.getActive());
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, Long outletId, CategoryRequest request) {
        MenuCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setActive(request.getActive());
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }

    @Transactional
    public ItemResponse createItem(Long outletId, ItemRequest request) {
        MenuCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));
        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setBasePriceMinor(request.getBasePriceMinor());
        item.setImageUrl(request.getImageUrl());
        item.setDietaryType(request.getDietaryType());
        item.setActive(request.getActive());
        item.setAvailable(request.getAvailable());
        MenuItem saved = itemRepository.save(item);
        return toItemResponse(saved, outletId);
    }

    @Transactional
    public ItemResponse updateItem(Long itemId, Long outletId, ItemRequest request) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
        MenuCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));
        item.setCategory(category);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setBasePriceMinor(request.getBasePriceMinor());
        item.setImageUrl(request.getImageUrl());
        item.setDietaryType(request.getDietaryType());
        item.setActive(request.getActive());
        item.setAvailable(request.getAvailable());
        MenuItem saved = itemRepository.save(item);
        return toItemResponse(saved, outletId);
    }

    @Transactional
    public void deleteItem(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new IllegalArgumentException("Item not found: " + itemId);
        }
        itemRepository.deleteById(itemId);
    }

    @Transactional
    public ModifierGroupResponse createModifierGroup(Long outletId, ModifierGroupRequest request) {
        ModifierGroup group = new ModifierGroup();
        group.setOutlet(loadOutlet(outletId));
        group.setName(request.getName());
        group.setMinSelect(request.getMinSelect());
        group.setMaxSelect(request.getMaxSelect());
        group.setRequired(request.getRequired());
        group.setActive(request.getActive());
        return toModifierGroupResponse(modifierGroupRepository.save(group));
    }

    @Transactional
    public ModifierGroupResponse updateModifierGroup(Long groupId, Long outletId, ModifierGroupRequest request) {
        ModifierGroup group = modifierGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Modifier group not found: " + groupId));
        group.setName(request.getName());
        group.setMinSelect(request.getMinSelect());
        group.setMaxSelect(request.getMaxSelect());
        group.setRequired(request.getRequired());
        group.setActive(request.getActive());
        return toModifierGroupResponse(modifierGroupRepository.save(group));
    }

    @Transactional
    public void deleteModifierGroup(Long groupId) {
        if (!modifierGroupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Modifier group not found: " + groupId);
        }
        modifierGroupRepository.deleteById(groupId);
    }

    @Transactional
    public ModifierOptionResponse createModifierOption(Long groupId, ModifierOptionRequest request) {
        ModifierGroup group = modifierGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Modifier group not found: " + groupId));
        ModifierOption option = new ModifierOption();
        option.setGroup(group);
        option.setName(request.getName());
        option.setPriceDeltaMinor(request.getPriceDeltaMinor());
        option.setActive(request.getActive());
        return toModifierOptionResponse(modifierOptionRepository.save(option));
    }

    @Transactional
    public ModifierOptionResponse updateModifierOption(Long optionId, ModifierOptionRequest request) {
        ModifierOption option = modifierOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Modifier option not found: " + optionId));
        option.setName(request.getName());
        option.setPriceDeltaMinor(request.getPriceDeltaMinor());
        option.setActive(request.getActive());
        return toModifierOptionResponse(modifierOptionRepository.save(option));
    }

    @Transactional
    public void deleteModifierOption(Long optionId) {
        if (!modifierOptionRepository.existsById(optionId)) {
            throw new IllegalArgumentException("Modifier option not found: " + optionId);
        }
        modifierOptionRepository.deleteById(optionId);
    }

    /* ---------- Mappers ---------- */

    private CategoryResponse toCategoryResponse(MenuCategory c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(),
                c.getDisplayOrder(), c.getActive());
    }

    private ItemResponse toItemResponse(MenuItem item, Long outletId) {
        List<ModifierGroupResponse> groups = modifierGroupRepository.findByOutletIdAndActiveTrue(outletId).stream()
                .map(g -> toModifierGroupResponseWithOptions(g))
                .collect(Collectors.toList());
        return new ItemResponse(item.getId(), item.getCategory().getId(),
                item.getCategory().getName(), item.getName(), item.getDescription(),
                item.getBasePriceMinor(), item.getImageUrl(), item.getDietaryType(),
                item.getActive(), item.getAvailable(), groups);
    }

    private ModifierGroupResponse toModifierGroupResponse(ModifierGroup g) {
        return toModifierGroupResponseWithOptions(g);
    }

    private ModifierGroupResponse toModifierGroupResponseWithOptions(ModifierGroup g) {
        List<ModifierOptionResponse> options = modifierOptionRepository.findByGroupIdAndActiveTrue(g.getId()).stream()
                .map(o -> new ModifierOptionResponse(o.getId(), o.getName(), o.getPriceDeltaMinor(), o.getActive()))
                .collect(Collectors.toList());
        return new ModifierGroupResponse(g.getId(), g.getName(), g.getMinSelect(), g.getMaxSelect(),
                g.getRequired(), g.getActive(), options);
    }

    private ModifierOptionResponse toModifierOptionResponse(ModifierOption o) {
        return new ModifierOptionResponse(o.getId(), o.getName(), o.getPriceDeltaMinor(), o.getActive());
    }

    private com.restru.auth.entity.Outlet loadOutlet(Long outletId) {
        return outletRepository.findById(outletId)
                .orElseThrow(() -> new IllegalArgumentException("Outlet not found: " + outletId));
    }
}
