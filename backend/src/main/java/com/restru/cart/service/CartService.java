package com.restru.cart.service;

import com.restru.auth.repository.OutletRepository;
import com.restru.auth.entity.User;
import com.restru.auth.repository.UserRepository;
import com.restru.cart.dto.AddToCartRequest;
import com.restru.cart.dto.CartItemModifierResponse;
import com.restru.cart.dto.CartItemResponse;
import com.restru.cart.dto.CartResponse;
import com.restru.cart.dto.CheckoutQuoteResponse;
import com.restru.cart.entity.Cart;
import com.restru.cart.entity.CartItem;
import com.restru.cart.entity.CartItemModifier;
import com.restru.cart.repository.CartItemModifierRepository;
import com.restru.cart.repository.CartItemRepository;
import com.restru.cart.repository.CartRepository;
import com.restru.menu.entity.MenuItem;
import com.restru.menu.entity.MenuCategory;
import com.restru.menu.entity.ModifierOption;
import com.restru.menu.repository.MenuCategoryRepository;
import com.restru.menu.repository.MenuItemRepository;
import com.restru.menu.repository.ModifierOptionRepository;
import com.restru.menu.repository.OutletItemAvailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cart business logic with server-side authoritative pricing.
 * Prices are always recalculated from the database; client-supplied prices are never trusted.
 */
@Service
@Transactional(readOnly = true)
public class CartService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.05"); // 5% tax
    private static final Integer DELIVERY_FEE_MINOR = 4000; // ₹40.00

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final OutletRepository outletRepository;
    private final ModifierOptionRepository modifierOptionRepository;
    private final OutletItemAvailabilityRepository availabilityRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       CartItemModifierRepository cartItemModifierRepository,
                       UserRepository userRepository,
                       MenuItemRepository menuItemRepository,
                       MenuCategoryRepository menuCategoryRepository,
                       OutletRepository outletRepository,
                       ModifierOptionRepository modifierOptionRepository,
                       OutletItemAvailabilityRepository availabilityRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.menuItemRepository = menuItemRepository;
        this.menuCategoryRepository = menuCategoryRepository;
        this.outletRepository = outletRepository;
        this.modifierOptionRepository = modifierOptionRepository;
        this.availabilityRepository = availabilityRepository;
    }

    /* ---------- Resolve user ---------- */

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + email));
    }

    /* ---------- Public read: get cart with authoritative pricing ---------- */

    public CartResponse getCart(String email) {
        User user = resolveUser(email);

        Cart cart = cartRepository.findByCustomerId(user.getId()).orElse(null);
        if (cart == null) {
            return emptyCart(user.getId(), null);
        }

        // Load items explicitly via repository to avoid first-level cache / bidirectional
        // relationship staleness within the same persistence context.
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());

        PricingResult pricing = calculatePricing(itemResponses);

        return new CartResponse(
                cart.getId(), cart.getCustomer().getId(),
                cart.getOutlet() != null ? cart.getOutlet().getId() : null,
                itemResponses,
                pricing.subtotalMinor, pricing.taxMinor, pricing.deliveryFeeMinor,
                pricing.grandTotalMinor, pricing.itemCount
        );
    }

    /* ---------- Mutations ---------- */

    @Transactional
    public CartResponse addToCart(String email, AddToCartRequest request) {
        User user = resolveUser(email);

        MenuItem item = menuItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + request.getItemId()));

        if (!item.getActive() || !item.getAvailable()) {
            throw new IllegalArgumentException("Item is not available: " + item.getName());
        }

        Cart cart = cartRepository.findByCustomerId(user.getId())
                .orElseGet(() -> createCart(user));

        // Enforce single-outlet cart — load outlet explicitly to avoid lazy-loading issues
        if (cart.getOutlet() == null) {
            MenuCategory category = menuCategoryRepository.findById(item.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            com.restru.auth.entity.Outlet itemOutlet = category.getOutlet() != null
                    ? outletRepository.findById(category.getOutlet().getId()).orElse(null)
                    : null;
            cart.setOutlet(itemOutlet);
        }
        cart = cartRepository.save(cart);

        // Find existing cart item (load explicitly to avoid stale first-level cache)
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        CartItem existing = cartItems.stream()
                .filter(ci -> ci.getItem().getId().equals(request.getItemId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            cartItemRepository.save(existing);
            replaceModifiers(existing, request.getModifierOptionIds());
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setItem(item);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setInstructions(request.getInstructions());
            CartItem saved = cartItemRepository.save(cartItem);
            replaceModifiers(saved, request.getModifierOptionIds());
        }

        return getCart(email);
    }

    @Transactional
    public CartResponse updateCartItemQuantity(String email, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        Cart cart = cartItem.getCart();
        if (!cart.getCustomer().getEmail().equals(email)) {
            throw new IllegalArgumentException("Cart item does not belong to this customer");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return getCart(email);
    }

    @Transactional
    public CartResponse removeCartItem(String email, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        Cart cart = cartItem.getCart();
        if (!cart.getCustomer().getEmail().equals(email)) {
            throw new IllegalArgumentException("Cart item does not belong to this customer");
        }

        cartItemRepository.delete(cartItem);
        return getCart(email);
    }

    @Transactional
    public CartResponse clearCart(String email) {
        User user = resolveUser(email);
        Cart cart = cartRepository.findByCustomerId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for customer: " + email));
        cart.getItems().clear();
        cartRepository.save(cart);
        return getCart(email);
    }

    /* ---------- Checkout quote (authoritative pricing, no order placed) ---------- */

    public CheckoutQuoteResponse quote(String email) {
        CartResponse cart = getCart(email);
        return new CheckoutQuoteResponse(
                cart.getSubtotalMinor(), cart.getTaxMinor(), cart.getDeliveryFeeMinor(),
                cart.getGrandTotalMinor(), cart.getItemCount()
        );
    }

    /* ---------- Pricing engine (server-side authoritative) ---------- */

    private PricingResult calculatePricing(List<CartItemResponse> items) {
        int subtotalMinor = 0;
        int itemCount = 0;

        for (CartItemResponse it : items) {
            int lineTotal = it.getLineTotalMinor() != null ? it.getLineTotalMinor() : 0;
            subtotalMinor += lineTotal;
            itemCount += it.getQuantity();
        }

        int taxMinor = roundTax(subtotalMinor);
        int grandTotalMinor = subtotalMinor + taxMinor + DELIVERY_FEE_MINOR;

        return new PricingResult(subtotalMinor, taxMinor, DELIVERY_FEE_MINOR, grandTotalMinor, itemCount);
    }

    private int roundTax(int subtotalMinor) {
        BigDecimal tax = BigDecimal.valueOf(subtotalMinor)
                .multiply(TAX_RATE)
                .setScale(0, RoundingMode.HALF_UP);
        return tax.intValue();
    }

    private CartResponse emptyCart(Long customerId, Long outletId) {
        return new CartResponse(null, customerId, outletId, List.of(), 0, 0, DELIVERY_FEE_MINOR, DELIVERY_FEE_MINOR, 0);
    }

    /* ---------- Helpers ---------- */

    private Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setCustomer(user);
        cart.setOutlet(null);
        return cartRepository.save(cart);
    }

    private void replaceModifiers(CartItem cartItem, List<Long> modifierOptionIds) {
        cartItem.getModifiers().clear();
        if (modifierOptionIds == null || modifierOptionIds.isEmpty()) {
            cartItemRepository.save(cartItem);
            return;
        }
        for (Long optionId : modifierOptionIds) {
            ModifierOption option = modifierOptionRepository.findById(optionId)
                    .orElseThrow(() -> new IllegalArgumentException("Modifier option not found: " + optionId));
            CartItemModifier cim = new CartItemModifier();
            cim.setCartItem(cartItem);
            cim.setModifierOption(option);
            cim.setQuantity(1);
            cartItem.getModifiers().add(cim);
        }
        cartItemRepository.save(cartItem);
    }

    private CartItemResponse toCartItemResponse(CartItem ci) {
        MenuItem item = ci.getItem();
        Integer basePrice = resolveBasePrice(item, ci.getCart().getOutlet());

        List<CartItemModifierResponse> modifierResponses = ci.getModifiers().stream()
                .map(cim -> {
                    ModifierOption opt = cim.getModifierOption();
                    Integer delta = opt.getPriceDeltaMinor() != null ? opt.getPriceDeltaMinor() : 0;
                    Integer lineTotal = (basePrice + delta) * cim.getQuantity();
                    return new CartItemModifierResponse(
                            opt.getId(), opt.getName(), delta, cim.getQuantity(), lineTotal
                    );
                })
                .collect(Collectors.toList());

        Integer modifierTotal = modifierResponses.stream()
                .mapToInt(m -> m.getLineTotalMinor())
                .sum();
        Integer lineTotal = (basePrice * ci.getQuantity()) + modifierTotal;

        return new CartItemResponse(
                ci.getId(), item.getId(), item.getName(), basePrice,
                ci.getQuantity(), ci.getInstructions(), modifierResponses, lineTotal
        );
    }

    private Integer resolveBasePrice(MenuItem item, com.restru.auth.entity.Outlet outlet) {
        // Outlet price override takes precedence; otherwise base price from menu DB.
        if (outlet != null) {
            return availabilityRepository.findByOutletId(outlet.getId()).stream()
                    .filter(a -> a.getItem().getId().equals(item.getId()))
                    .filter(a -> a.getPriceOverrideMinor() != null)
                    .findFirst()
                    .map(a -> a.getPriceOverrideMinor())
                    .orElse(item.getBasePriceMinor());
        }
        return item.getBasePriceMinor();
    }

    private record PricingResult(int subtotalMinor, int taxMinor, int deliveryFeeMinor,
                                 int grandTotalMinor, int itemCount) {
    }
}
