package com.restru.menu.controller;

import com.restru.common.dto.ApiResponse;
import com.restru.menu.dto.CategoryRequest;
import com.restru.menu.dto.CategoryResponse;
import com.restru.menu.dto.ItemRequest;
import com.restru.menu.dto.ItemResponse;
import com.restru.menu.dto.ModifierGroupRequest;
import com.restru.menu.dto.ModifierGroupResponse;
import com.restru.menu.dto.ModifierOptionRequest;
import com.restru.menu.dto.ModifierOptionResponse;
import com.restru.menu.dto.ServiceabilityResponse;
import com.restru.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Menu endpoints.
 * Public read: GET /api/v1/menu/categories, GET /api/v1/menu/items
 * Admin write: POST/PUT/DELETE /api/v1/menu/admin/**
 */
@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /* ---------- Public read ---------- */

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories(
            @RequestParam(required = false) Long outletId) {

        List<CategoryResponse> categories = menuService.listCategories(outletId);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> listItems(
            @RequestParam(required = false) Long outletId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean all) {

        List<ItemResponse> items = menuService.listItems(outletId, categoryId, Boolean.TRUE.equals(all));
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<ItemResponse>> getItem(
            @PathVariable Long itemId,
            @RequestParam(required = false) Long outletId) {

        ItemResponse item = menuService.getItem(itemId, outletId);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    /* ---------- Public: outlet serviceability ---------- */

    @GetMapping("/outlets/serviceability")
    public ResponseEntity<ApiResponse<ServiceabilityResponse>> checkServiceability(
            @RequestParam String pincode,
            @RequestParam(required = false) Long outletId) {

        ServiceabilityResponse response = menuService.checkServiceability(pincode, outletId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /* ---------- Admin write (ADMIN) ---------- */

    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @RequestParam Long outletId,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse created = menuService.createCategory(outletId, request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Category created."));
    }

    @PutMapping("/admin/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @RequestParam Long outletId,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse updated = menuService.updateCategory(categoryId, outletId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Category updated."));
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long categoryId) {

        menuService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted."));
    }

    @PostMapping("/admin/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(
            @RequestParam Long outletId,
            @Valid @RequestBody ItemRequest request) {

        ItemResponse created = menuService.createItem(outletId, request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Item created."));
    }

    @PutMapping("/admin/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable Long itemId,
            @RequestParam Long outletId,
            @Valid @RequestBody ItemRequest request) {

        ItemResponse updated = menuService.updateItem(itemId, outletId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Item updated."));
    }

    @DeleteMapping("/admin/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long itemId) {

        menuService.deleteItem(itemId);
        return ResponseEntity.ok(ApiResponse.ok("Item deleted."));
    }

    @PostMapping("/admin/modifier-groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ModifierGroupResponse>> createModifierGroup(
            @RequestParam Long outletId,
            @Valid @RequestBody ModifierGroupRequest request) {

        ModifierGroupResponse created = menuService.createModifierGroup(outletId, request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Modifier group created."));
    }

    @PutMapping("/admin/modifier-groups/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ModifierGroupResponse>> updateModifierGroup(
            @PathVariable Long groupId,
            @RequestParam Long outletId,
            @Valid @RequestBody ModifierGroupRequest request) {

        ModifierGroupResponse updated = menuService.updateModifierGroup(groupId, outletId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Modifier group updated."));
    }

    @DeleteMapping("/admin/modifier-groups/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteModifierGroup(
            @PathVariable Long groupId) {

        menuService.deleteModifierGroup(groupId);
        return ResponseEntity.ok(ApiResponse.ok("Modifier group deleted."));
    }

    @PostMapping("/admin/modifier-groups/{groupId}/options")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ModifierOptionResponse>> createModifierOption(
            @PathVariable Long groupId,
            @Valid @RequestBody ModifierOptionRequest request) {

        ModifierOptionResponse created = menuService.createModifierOption(groupId, request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Modifier option created."));
    }

    @PutMapping("/admin/modifier-options/{optionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ModifierOptionResponse>> updateModifierOption(
            @PathVariable Long optionId,
            @Valid @RequestBody ModifierOptionRequest request) {

        ModifierOptionResponse updated = menuService.updateModifierOption(optionId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Modifier option updated."));
    }

    @DeleteMapping("/admin/modifier-options/{optionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteModifierOption(
            @PathVariable Long optionId) {

        menuService.deleteModifierOption(optionId);
        return ResponseEntity.ok(ApiResponse.ok("Modifier option deleted."));
    }
}
