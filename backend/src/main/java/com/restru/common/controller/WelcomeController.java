package com.restru.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root welcome endpoint showing backend status and frontend URL.
 */
@RestController
public class WelcomeController {

    @GetMapping("/")
    public Map<String, Object> welcome() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "Restru Backend");
        response.put("status", "RUNNING");
        response.put("version", "1.0.0-SNAPSHOT");
        response.put("backend_url", "http://localhost:8080");
        response.put("frontend_url", "http://localhost:5173");
        response.put("health_endpoint", "http://localhost:8080/api/v1/health");
        response.put("message", "Backend is running. Open the frontend URL in your browser to use the application.");
        return response;
    }
}
