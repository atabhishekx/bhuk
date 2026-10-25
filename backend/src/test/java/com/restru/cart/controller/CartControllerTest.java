package com.restru.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restru.auth.entity.User;
import com.restru.auth.entity.UserRole;
import com.restru.auth.repository.UserRepository;
import com.restru.auth.repository.UserRoleRepository;
import com.restru.cart.dto.AddToCartRequest;
import com.restru.menu.entity.MenuCategory;
import com.restru.menu.entity.MenuItem;
import com.restru.menu.repository.MenuCategoryRepository;
import com.restru.menu.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for cart endpoints with server-side pricing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MenuCategoryRepository categoryRepository;

    @Autowired
    private MenuItemRepository itemRepository;

    private String customerEmail;
    private Long itemId;

    @BeforeEach
    void setUp() {
        customerEmail = "cart.user." + System.currentTimeMillis() + "@test.local";

        User user = new User();
        user.setEmail(customerEmail);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("Cart User");
        user.setStatus("ACTIVE");
        user = userRepository.save(user);

        UserRole role = new UserRole();
        role.setUser(user);
        role.setRole("USER");
        role.setOutlet(null);
        userRoleRepository.save(role);

        MenuCategory category = new MenuCategory();
        category.setName("Test Category");
        category.setDisplayOrder(1);
        category.setActive(true);
        category = categoryRepository.save(category);

        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setName("Test Item");
        item.setDescription("A test item");
        item.setBasePriceMinor(10000); // ₹100.00
        item.setActive(true);
        item.setAvailable(true);
        item = itemRepository.save(item);

        itemId = item.getId();
    }

    private String body(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String loginAsCustomer() throws Exception {
        String loginJson = "{\"email\":\"" + customerEmail + "\",\"password\":\"password123\"}";
        String result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(result).path("data").path("token").asText();
    }

    @Test
    void addToCartAndGetCartReturnsPricing() throws Exception {
        String token = loginAsCustomer();

        AddToCartRequest request = new AddToCartRequest();
        request.setItemId(itemId);
        request.setQuantity(2);

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items[0].itemName").value("Test Item"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.items[0].lineTotalMinor").value(20000));

        // Get cart - should show pricing
        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemCount").value(2))
                .andExpect(jsonPath("$.data.subtotalMinor").value(20000))
                .andExpect(jsonPath("$.data.taxMinor").value(1000)) // 5% of 20000
                .andExpect(jsonPath("$.data.deliveryFeeMinor").value(4000))
                .andExpect(jsonPath("$.data.grandTotalMinor").value(25000)); // 20000 + 1000 + 4000
    }

    @Test
    void checkoutQuoteReturnsAuthoritativePricing() throws Exception {
        String token = loginAsCustomer();

        // Empty cart quote
        mockMvc.perform(get("/api/v1/cart/checkout/quote")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemCount").value(0))
                .andExpect(jsonPath("$.data.grandTotalMinor").value(4000)); // just delivery fee

        // Add item then quote
        AddToCartRequest request = new AddToCartRequest();
        request.setItemId(itemId);
        request.setQuantity(1);

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/cart/checkout/quote")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subtotalMinor").value(10000))
                .andExpect(jsonPath("$.data.taxMinor").value(500))
                .andExpect(jsonPath("$.data.grandTotalMinor").value(14500)); // 10000 + 500 + 4000
    }

    @Test
    void updateQuantityAndRemoveItem() throws Exception {
        String token = loginAsCustomer();

        AddToCartRequest request = new AddToCartRequest();
        request.setItemId(itemId);
        request.setQuantity(1);

        String addResult = mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long cartItemId = objectMapper.readTree(addResult)
                .path("data").path("items").path(0).path("id").asLong();

        // Update quantity to 3
        mockMvc.perform(put("/api/v1/cart/items/" + cartItemId + "/quantity")
                        .header("Authorization", "Bearer " + token)
                        .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].quantity").value(3))
                .andExpect(jsonPath("$.data.items[0].lineTotalMinor").value(30000));

        // Remove item
        mockMvc.perform(delete("/api/v1/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    @Test
    void cartWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().is4xxClientError());
    }
}
