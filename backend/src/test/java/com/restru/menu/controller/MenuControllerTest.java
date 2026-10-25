package com.restru.menu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restru.auth.entity.Outlet;
import com.restru.auth.entity.User;
import com.restru.auth.entity.UserRole;
import com.restru.auth.repository.OutletRepository;
import com.restru.auth.repository.UserRepository;
import com.restru.auth.repository.UserRoleRepository;
import com.restru.menu.dto.CategoryRequest;
import com.restru.menu.dto.ItemRequest;
import com.restru.menu.dto.ModifierGroupRequest;
import com.restru.menu.dto.ModifierOptionRequest;
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
 * Integration tests for menu endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MenuControllerTest {

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
    private PasswordEncoder passwordEncoder;

    private String staffToken;
    private Long outletId;

    @BeforeEach
    void setUp() throws Exception {
        // Outlet
        Outlet outlet = new Outlet();
        outlet.setName("Restru Main Branch");
        outlet.setAddress("123 Main St");
        outlet.setCity("City");
        outlet.setState("State");
        outlet.setPincode("110001");
        outlet.setTimezone("Asia/Kolkata");
        outlet.setStatus("ACTIVE");
        outlet = outletRepository.save(outlet);
        outletId = outlet.getId();

        // Staff user
        String email = "staff." + System.currentTimeMillis() + "@test.local";
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("Staff User");
        user.setStatus("ACTIVE");
        user = userRepository.save(user);

        UserRole role = new UserRole();
        role.setUser(user);
        role.setRole("ADMIN");
        role.setOutlet(outlet);
        userRoleRepository.save(role);

        // Login as staff
        String loginJson = "{\"email\":\"" + email + "\",\"password\":\"password123\"}";
        String result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        staffToken = objectMapper.readTree(result).path("data").path("token").asText();
    }

    private String body(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    void publicListCategoriesAndItems() throws Exception {
        // Create a category as staff
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Beverages");
        catReq.setDisplayOrder(1);
        catReq.setActive(true);

        String catResult = mockMvc.perform(post("/api/v1/menu/admin/categories")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("outletId", outletId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(catReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long categoryId = objectMapper.readTree(catResult)
                .path("data").path("id").asLong();

        // Create an item
        ItemRequest itemReq = new ItemRequest();
        itemReq.setCategoryId(categoryId);
        itemReq.setName("Cold Coffee");
        itemReq.setDescription("Iced coffee");
        itemReq.setBasePriceMinor(9900);
        itemReq.setActive(true);
        itemReq.setAvailable(true);

        mockMvc.perform(post("/api/v1/menu/admin/items")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("outletId", outletId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(itemReq)))
                .andExpect(status().isCreated());

        // Public list categories
        mockMvc.perform(get("/api/v1/menu/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Public list items
        mockMvc.perform(get("/api/v1/menu/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void adminCreateModifierGroupAndOption() throws Exception {
        ModifierGroupRequest groupReq = new ModifierGroupRequest();
        groupReq.setName("Spice Level");
        groupReq.setMinSelect(0);
        groupReq.setMaxSelect(1);
        groupReq.setRequired(false);
        groupReq.setActive(true);

        String groupResult = mockMvc.perform(post("/api/v1/menu/admin/modifier-groups")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("outletId", outletId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(groupReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long groupId = objectMapper.readTree(groupResult)
                .path("data").path("id").asLong();

        ModifierOptionRequest optReq = new ModifierOptionRequest();
        optReq.setName("Spicy");
        optReq.setPriceDeltaMinor(0);
        optReq.setActive(true);

        mockMvc.perform(post("/api/v1/menu/admin/modifier-groups/" + groupId + "/options")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(optReq)))
                .andExpect(status().isCreated());
    }

    @Test
    void adminEndpointsRequireAuth() throws Exception {
        CategoryRequest catReq = new CategoryRequest();
        catReq.setName("Test");

        mockMvc.perform(post("/api/v1/menu/admin/categories")
                        .param("outletId", outletId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(catReq)))
                .andExpect(status().is4xxClientError());
    }
}
