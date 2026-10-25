package com.restru.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the auth endpoints.
 * Uses unique emails per invocation so tests are repeatable against a shared dev DB.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String uniqueEmail(String prefix) {
        return prefix + System.currentTimeMillis() + "@test.local";
    }

    private String register(String email, String password, String name) throws Exception {
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password +
                "\",\"name\":\"" + name + "\"}";
        return mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void registerNewUserReturnsCreated() throws Exception {
        String email = uniqueEmail("register");
        String json = "{\"email\":\"" + email + "\",\"password\":\"password123\",\"name\":\"Register Tester\",\"phone\":\"+91-9999999996\"}";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andExpect(jsonPath("$.data.user.roles[0]").value("USER"));
    }

    @Test
    void registerDuplicateEmailReturnsBadRequest() throws Exception {
        String email = uniqueEmail("dup");
        register(email, "password123", "Dup Tester");

        String json = "{\"email\":\"" + email + "\",\"password\":\"password123\",\"name\":\"Dup Tester\"}";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsToken() throws Exception {
        String email = uniqueEmail("login");
        register(email, "password123", "Login Tester");

        String loginJson = "{\"email\":\"" + email + "\",\"password\":\"password123\"}";
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void registerDeliveryGuyReturnsUserRole() throws Exception {
        String email = uniqueEmail("delivery");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\",\"name\":\"Delivery Tester\",\"role\":\"DELIVERY_GUY\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.roles[0]").value("DELIVERY_GUY"));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        String email = uniqueEmail("wrongpass");
        register(email, "password123", "Wrong Pass Tester");

        String loginJson = "{\"email\":\"" + email + "\",\"password\":\"wrongpassword\"}";
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsProfileWhenAuthenticated() throws Exception {
        String email = uniqueEmail("me");
        register(email, "password123", "Me Tester");

        String loginJson = "{\"email\":\"" + email + "\",\"password\":\"password123\"}";
        String token = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract token from JSON response
        String tok = new ObjectMapper().readTree(token).path("data").path("token").asText();

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + tok))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.roles[0]").value("USER"));
    }

    @Test
    void meReturnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().is4xxClientError());
    }
}
