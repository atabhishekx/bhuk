package com.restru.delivery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restru.auth.entity.User;
import com.restru.auth.entity.UserRole;
import com.restru.auth.repository.OutletRepository;
import com.restru.auth.repository.UserRepository;
import com.restru.auth.repository.UserRoleRepository;
import com.restru.delivery.entity.DeliveryPartner;
import com.restru.delivery.repository.DeliveryPartnerRepository;
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
 * Integration tests for delivery partner and tracking endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class DeliveryControllerTest {

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
    private DeliveryPartnerRepository partnerRepository;

    @Autowired
    private MenuCategoryRepository categoryRepository;

    @Autowired
    private MenuItemRepository itemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String partnerEmail;
    private String customerEmail;
    private String staffEmail;
    private Long itemId;
    private Long orderId;
    private Long assignmentId;

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

        // Partner user
        partnerEmail = "partner." + System.currentTimeMillis() + "@test.local";
        User partnerUser = new User();
        partnerUser.setEmail(partnerEmail);
        partnerUser.setPassword(passwordEncoder.encode("password123"));
        partnerUser.setName("Delivery Partner");
        partnerUser.setStatus("ACTIVE");
        partnerUser = userRepository.save(partnerUser);

        UserRole partnerRole = new UserRole();
        partnerRole.setUser(partnerUser);
        partnerRole.setRole("DELIVERY_GUY");
        partnerRole.setOutlet(outlet);
        userRoleRepository.save(partnerRole);

        // DeliveryPartner profile
        DeliveryPartner dp = new DeliveryPartner();
        dp.setUser(partnerUser);
        dp.setOutlet(outlet);
        dp.setStatus("OFFLINE");
        partnerRepository.save(dp);

        // Customer
        customerEmail = "delivery.customer." + System.currentTimeMillis() + "@test.local";
        User customer = new User();
        customer.setEmail(customerEmail);
        customer.setPassword(passwordEncoder.encode("password123"));
        customer.setName("Delivery Customer");
        customer.setStatus("ACTIVE");
        customer = userRepository.save(customer);

        UserRole customerRole = new UserRole();
        customerRole.setUser(customer);
        customerRole.setRole("USER");
        customerRole.setOutlet(null);
        userRoleRepository.save(customerRole);

        // Staff
        staffEmail = "delivery.staff." + System.currentTimeMillis() + "@test.local";
        User staff = new User();
        staff.setEmail(staffEmail);
        staff.setPassword(passwordEncoder.encode("password123"));
        staff.setName("Delivery Staff");
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
        item.setBasePriceMinor(20000);
        item.setActive(true);
        item.setAvailable(true);
        item = itemRepository.save(item);

        itemId = item.getId();

        // Create order and advance to READY_FOR_PICKUP
        String customerToken = loginAsUser();

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":" + itemId + ",\"quantity\":1}"))
                .andExpect(status().isCreated());

        CreateOrderRequest request = new CreateOrderRequest();
        request.setIdempotencyKey("delivery-order-key-" + System.currentTimeMillis());
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
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        orderId = objectMapper.readTree(createResult).path("data").path("id").asLong();

        String staffToken = loginAsAdmin();

        // Staff accepts order
        mockMvc.perform(post("/api/v1/staff/orders/" + orderId + "/accept")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());

        // Staff advances to PREPARING
        mockMvc.perform(post("/api/v1/staff/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("newStatus", "PREPARING")
                        .param("reason", "Started preparing"))
                .andExpect(status().isOk());

        // Staff advances to READY_FOR_PICKUP
        mockMvc.perform(post("/api/v1/staff/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("newStatus", "READY_FOR_PICKUP")
                        .param("reason", "Order ready for delivery"))
                .andExpect(status().isOk());
    }

    private String loginAsDeliveryGuy() throws Exception {
        String loginJson = "{\"email\":\"" + partnerEmail + "\",\"password\":\"password123\"}";
        String result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(result).path("data").path("token").asText();
    }

    private String loginAsUser() throws Exception {
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
    void setPartnerAvailability() throws Exception {
        String token = loginAsDeliveryGuy();

        mockMvc.perform(patch("/api/v1/partner/availability")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ONLINE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ONLINE"));
    }

    @Test
    void listPartnerAssignments() throws Exception {
        String token = loginAsDeliveryGuy();

        mockMvc.perform(get("/api/v1/partner/assignments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void acceptAndCompleteDelivery() throws Exception {
        String token = loginAsDeliveryGuy();

        // Set online first
        mockMvc.perform(patch("/api/v1/partner/availability")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ONLINE\"}"))
                .andExpect(status().isOk());

        // Get the auto-created OFFERED assignment
        String listResult = mockMvc.perform(get("/api/v1/partner/assignments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assignmentId = objectMapper.readTree(listResult).path("data").path(0).path("id").asLong();
        String state = objectMapper.readTree(listResult).path("data").path(0).path("state").asText();
        org.junit.jupiter.api.Assertions.assertEquals("OFFERED", state);

        // Accept assignment
        mockMvc.perform(post("/api/v1/partner/assignments/" + assignmentId + "/accept")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("ASSIGNED"));

        // Mark arrived
        mockMvc.perform(post("/api/v1/partner/deliveries/" + assignmentId + "/arrived")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("ACCEPTED"));

        // Mark picked up
        mockMvc.perform(post("/api/v1/partner/deliveries/" + assignmentId + "/pickup")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("PICKED_UP"));

        // Update location
        mockMvc.perform(post("/api/v1/partner/deliveries/" + assignmentId + "/location")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lat\":28.6139,\"lng\":77.2090,\"accuracy\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lat").value(28.6139));

        // Mark completed
        mockMvc.perform(post("/api/v1/partner/deliveries/" + assignmentId + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("COMPLETED"));
    }

    @Test
    void declineAssignmentRequiresReason() throws Exception {
        String token = loginAsDeliveryGuy();

        // Get the auto-created OFFERED assignment
        String listResult = mockMvc.perform(get("/api/v1/partner/assignments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long aid = objectMapper.readTree(listResult).path("data").path(0).path("id").asLong();

        // Decline without reason → 400
        mockMvc.perform(post("/api/v1/partner/assignments/" + aid + "/decline")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        // Decline with reason → 200
        mockMvc.perform(post("/api/v1/partner/assignments/" + aid + "/decline")
                        .header("Authorization", "Bearer " + token)
                        .param("reason", "Too far"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("DECLINED"));
    }

    @Test
    void reportDeliveryIssue() throws Exception {
        String token = loginAsDeliveryGuy();

        // Get the auto-created OFFERED assignment
        String listResult = mockMvc.perform(get("/api/v1/partner/assignments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long aid = objectMapper.readTree(listResult).path("data").path(0).path("id").asLong();

        // Accept first
        mockMvc.perform(post("/api/v1/partner/assignments/" + aid + "/accept")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Report issue
        mockMvc.perform(post("/api/v1/partner/deliveries/" + aid + "/issue")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"issueType\":\"CUSTOMER_NOT_FOUND\",\"description\":\"Customer not answering phone\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("FAILED"));
    }

    @Test
    void customerOrderTracking() throws Exception {
        String token = loginAsUser();

        mockMvc.perform(get("/api/v1/track/orders/" + orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(orderId.intValue()))
                .andExpect(jsonPath("$.data.orderStatus").exists());
    }

    @Test
    void staffOrderQueue() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(get("/api/v1/staff/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void partnerEndpointsRequirePartnerRole() throws Exception {
        String customerToken = loginAsUser();

        mockMvc.perform(patch("/api/v1/partner/availability")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ONLINE\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void adminListsAvailablePartnersAndAssigns() throws Exception {
        // Delivery partner goes online
        String partnerToken = loginAsDeliveryGuy();
        mockMvc.perform(patch("/api/v1/partner/availability")
                        .header("Authorization", "Bearer " + partnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ONLINE\"}"))
                .andExpect(status().isOk());

        String adminToken = loginAsAdmin();

        // Admin lists available partners
        String listResult = mockMvc.perform(get("/api/v1/admin/delivery-partners/available")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        java.util.List<Long> partnerIds = objectMapper.readTree(listResult)
                .path("data").findValues("id").stream().map(json -> json.asLong()).toList();
        org.junit.jupiter.api.Assertions.assertFalse(partnerIds.isEmpty(), "Expected at least one available partner");

        Long partnerId = partnerIds.get(0);

        // Admin assigns the partner to the order
        mockMvc.perform(post("/api/v1/admin/deliveries/{orderId}/assign", orderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("partnerId", partnerId.toString())
                        .param("reason", "Admin manual assignment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("ASSIGNED"))
                .andExpect(jsonPath("$.data.partnerId").value(partnerId.intValue()));
    }
}
