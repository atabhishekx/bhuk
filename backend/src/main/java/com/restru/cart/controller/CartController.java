package com.restru.cart.controller;

import com.restru.cart.dto.AddToCartRequest;
import com.restru.cart.dto.CartResponse;
import com.restru.cart.dto.CheckoutQuoteResponse;
import com.restru.cart.service.CartService;
import com.restru.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Cart endpoints.
 * GET /api/v1/cart — view cart with authoritative pricing
 * POST /api/v1/cart/items — add item
 * PUT /api/v1/cart/items/{id}/quantity — update quantity
 * DELETE /api/v1/cart/items/{id} — remove item
 * DELETE /api/v1/cart — clear cart
 * GET /api/v1/cart/checkout/quote — authoritative checkout quote
 */
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails principal) {

        CartResponse cart = cartService.getCart(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AddToCartRequest request) {

        CartResponse cart = cartService.addToCart(principal.getUsername(), request);
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(cart, "Item added to cart."));
    }

    @PutMapping("/items/{cartItemId}/quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {

        CartResponse cart = cartService.updateCartItemQuantity(principal.getUsername(), cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart updated."));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long cartItemId) {

        CartResponse cart = cartService.removeCartItem(principal.getUsername(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item removed."));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(
            @AuthenticationPrincipal UserDetails principal) {

        CartResponse cart = cartService.clearCart(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart cleared."));
    }

    @GetMapping("/checkout/quote")
    public ResponseEntity<ApiResponse<CheckoutQuoteResponse>> checkoutQuote(
            @AuthenticationPrincipal UserDetails principal) {

        CheckoutQuoteResponse quote = cartService.quote(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(quote, "Checkout quote."));
    }
}
