package com.restru.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restru.auth.entity.User;
import com.restru.auth.entity.UserRole;
import com.restru.auth.repository.OutletRepository;
import com.restru.auth.repository.UserRepository;
import com.restru.auth.repository.UserRoleRepository;
import com.restru.menu.entity.MenuCategory;
import com.restru.menu.entity.MenuItem;
import com.restru.menu.repository.MenuCategoryRepository;
import com.restru.menu.repository.MenuItemRepository;
import com.restru.order.dto.CreateOrderRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for order lifecycle endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private OutletRepository outletRepository;

    @Autowired
    private MenuCategoryRepository categoryRepository;

    @Autowired
    private MenuItemRepository itemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String customerEmail;
    private String staffEmail;
    private Long itemId;

    @BeforeEach
    void setUp() throws Exception {
        // Outlet
        com.restru.auth.entity.Outlet outlet = new com.restru.auth.entity.Outlet();
        outlet.setName("Restru Main Branch");
        outlet.setAddress("123 Main St");
        outlet.setCity("City");
        outlet.setState("State");
        outlet.setPincode("110001");
        outlet.setTimezone("Asia/Kolkata");
        outlet.setStatus("ACTIVE");
        outlet = outletRepository.save(outlet);
        outlet.getId();

        // Customer
        customerEmail = "order.customer." + System.currentTimeMillis() + "@test.local";
        User customer = new User();
        customer.setEmail(customerEmail);
        customer.setPassword(passwordEncoder.encode("password123"));
        customer.setName("Order Customer");
        customer.setStatus("ACTIVE");
        customer = userRepository.save(customer);

        UserRole customerRole = new UserRole();
        customerRole.setUser(customer);
        customerRole.setRole("USER");
        customerRole.setOutlet(null);
        userRoleRepository.save(customerRole);

        // Staff
        staffEmail = "order.staff." + System.currentTimeMillis() + "@test.local";
        User staff = new User();
        staff.setEmail(staffEmail);
        staff.setPassword(passwordEncoder.encode("password123"));
        staff.setName("Order Staff");
        staff.setStatus("ACTIVE");
        staff = userRepository.save(staff);

        UserRole staffRole = new UserRole();
        staffRole.setUser(staff);
        staffRole.setRole("ADMIN");
        staffRole.setOutlet(outlet);
        userRoleRepository.save(staffRole);

        // Category + item
        MenuCategory category = new MenuCategory();
        category.setOutlet(outlet);
        category.setName("Test Category");
        category.setDisplayOrder(1);
        category.setActive(true);
        category = categoryRepository.save(category);

        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setName("Test Burger");
        item.setDescription("A test burger");
        item.setBasePriceMinor(20000); // ₹200.00
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

    private String loginAsAdmin() throws Exception {
        String loginJson = "{\"email\":\"" + staffEmail + "\",\"password\":\"password123\"}";
        String result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(result).path("data").path("token").asText();
    }

    @Test
    void createOrderIdempotentAndList() throws Exception {
        String token = loginAsCustomer();

        // Add item to cart first
        String addToCart = "{\"itemId\":" + itemId + ",\"quantity\":2}";
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addToCart))
                .andExpect(status().isCreated());

        // Create order (idempotent)
        CreateOrderRequest request = new CreateOrderRequest();
        request.setIdempotencyKey("order-key-" + System.currentTimeMillis());
        CreateOrderRequest.DeliveryAddress addr = new CreateOrderRequest.DeliveryAddress();
        addr.setFullAddress("123 Customer Street, City");
        addr.setCity("City");
        addr.setState("State");
        addr.setPincode("110001");
        addr.setPhone("+91-9999999999");
        request.setDeliveryAddress(addr);

        String createResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderNumber").exists())
                .andExpect(jsonPath("$.data.status").value("PLACED"))
                .andExpect(jsonPath("$.data.grandTotalMinor").value(46000)) // 2*20000 + 2000 tax + 4000 delivery
                .andReturn().getResponse().getContentAsString();

        String orderNumber = objectMapper.readTree(createResult).path("data").path("orderNumber").asText();
        Long orderId = objectMapper.readTree(createResult).path("data").path("id").asLong();

        // Idempotent: same key should return same order
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderNumber").value(orderNumber));

        // List orders
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].orderNumber").value(orderNumber));

        // Get order detail
        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNumber").value(orderNumber))
                .andExpect(jsonPath("$.data.items[0].itemNameSnapshot").value("Test Burger"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.items[0].lineTotalMinor").value(40000));

        // Cancel order
        mockMvc.perform(post("/api/v1/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + token)
                        .param("reason", "Changed mind"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void staffAcceptAndAdvanceStatus() throws Exception {
        String customerToken = loginAsCustomer();

        // Add to cart + create order
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":" + itemId + ",\"quantity\":1}"))
                .andExpect(status().isCreated());

        CreateOrderRequest request = new CreateOrderRequest();
        request.setIdempotencyKey("staff-order-key-" + System.currentTimeMillis());
        CreateOrderRequest.DeliveryAddress addr = new CreateOrderRequest.DeliveryAddress();
        addr.setFullAddress("123 Customer Street");
        addr.setCity("City");
        addr.setState("State");
        addr.setPincode("110001");
        addr.setPhone("+91-9999999999");
        request.setDeliveryAddress(addr);

        String createResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResult).path("data").path("id").asLong();

        String staffToken = loginAsAdmin();

        // Staff accepts
        mockMvc.perform(post("/api/v1/staff/orders/" + orderId + "/accept")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        // Staff advances to PREPARING
        mockMvc.perform(post("/api/v1/staff/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("newStatus", "PREPARING")
                        .param("reason", "Started cooking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PREPARING"));

        // Staff advances to READY_FOR_PICKUP
        mockMvc.perform(post("/api/v1/staff/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("newStatus", "READY_FOR_PICKUP")
                        .param("reason", "Order ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READY_FOR_PICKUP"));

        // Staff handoff
        mockMvc.perform(post("/api/v1/staff/orders/" + orderId + "/handoff")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OUT_FOR_DELIVERY"));
    }

    @Test
    void staffRejectRequiresReason() throws Exception {
        String customerToken = loginAsCustomer();

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":" + itemId + ",\"quantity\":1}"))
                .andExpect(status().isCreated());

        CreateOrderRequest request = new CreateOrderRequest();
        request.setIdempotencyKey("reject-order-key-" + System.currentTimeMillis());
        CreateOrderRequest.DeliveryAddress addr = new CreateOrderRequest.DeliveryAddress();
        addr.setFullAddress("123 Customer Street");
        addr.setCity("City");
        addr.setState("State");
        addr.setPincode("110001");
        addr.setPhone("+91-9999999999");
        request.setDeliveryAddress(addr);

        String createResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResult).path("data").path("id").asLong();

        String staffToken = loginAsAdmin();

        // Reject without reason → 400
        mockMvc.perform(post("/api/v1/staff/orders/" + orderId + "/reject")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isBadRequest());

        // Reject with reason → 200
        mockMvc.perform(post("/api/v1/staff/orders/" + orderId + "/reject")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("reason", "Item out of stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    void invalidTransitionRejected() throws Exception {
        String customerToken = loginAsCustomer();

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":" + itemId + ",\"quantity\":1}"))
                .andExpect(status().isCreated());

        CreateOrderRequest request = new CreateOrderRequest();
        request.setIdempotencyKey("transition-key-" + System.currentTimeMillis());
        CreateOrderRequest.DeliveryAddress addr = new CreateOrderRequest.DeliveryAddress();
        addr.setFullAddress("123 Customer Street");
        addr.setCity("City");
        addr.setState("State");
        addr.setPincode("110001");
        addr.setPhone("+91-9999999999");
        request.setDeliveryAddress(addr);

        String createResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResult).path("data").path("id").asLong();

        String staffToken = loginAsAdmin();

        // Cannot go PLACED → DELIVERED directly
        mockMvc.perform(post("/api/v1/staff/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("newStatus", "DELIVERED")
                        .param("reason", "Skip steps"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrderMissingIdempotencyKey() throws Exception {
        String token = loginAsCustomer();

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryAddress\":{\"fullAddress\":\"test\"}}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void adminListsAllOrdersAndFiltersByStatus() throws Exception {
        // Admin user
        String adminEmail = "admin." + System.currentTimeMillis() + "@test.local";
        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setName("Admin User");
        admin.setStatus("ACTIVE");
        admin = userRepository.save(admin);
        UserRole adminRole = new UserRole();
        adminRole.setUser(admin);
        adminRole.setRole("ADMIN");
        adminRole.setOutlet(null);
        userRoleRepository.save(adminRole);

        String customerToken = loginAsCustomer();

        // Two orders from the customer
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/v1/cart/items")
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"itemId\":" + itemId + ",\"quantity\":1}"))
                    .andExpect(status().isCreated());

            CreateOrderRequest request = new CreateOrderRequest();
            request.setIdempotencyKey("admin-list-key-" + System.currentTimeMillis() + "-" + i);
            CreateOrderRequest.DeliveryAddress addr = new CreateOrderRequest.DeliveryAddress();
            addr.setFullAddress("123 Customer Street");
            addr.setCity("City");
            addr.setState("State");
            addr.setPincode("110001");
            addr.setPhone("+91-9999999999");
            request.setDeliveryAddress(addr);

            mockMvc.perform(post("/api/v1/orders")
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body(request)))
                    .andExpect(status().isCreated());
        }

        // Advance one order to PREPARING via staff
        String staffToken = loginAsAdmin();

        String freshCreate = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(new CreateOrderRequest() {{ setIdempotencyKey("fresh-key-" + System.currentTimeMillis()); }})))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long freshOrderId = objectMapper.readTree(freshCreate).path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/staff/orders/" + freshOrderId + "/accept")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/staff/orders/" + freshOrderId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("newStatus", "PREPARING"))
                .andExpect(status().isOk());

        String adminToken = loginAs(adminEmail);

        // Admin sees all orders
        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));

        // Admin filters by status
        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "PREPARING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("PREPARING"));

        // Admin searches by order number
        String orderNumber = mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "PREPARING"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String number = objectMapper.readTree(orderNumber).path("data").path("[0]").path("orderNumber").asText();

        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("q", number.substring(0, 4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    private String loginAs(String email) throws Exception {
        String loginJson = "{\"email\":\"" + email + "\",\"password\":\"password123\"}";
        String result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(result).path("data").path("token").asText();
    }
}
