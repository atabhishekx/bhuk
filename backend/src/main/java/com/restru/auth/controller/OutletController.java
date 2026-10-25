package com.restru.auth.controller;

import com.restru.auth.entity.Outlet;
import com.restru.auth.repository.OutletRepository;
import com.restru.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Outlet management for admins.
 * GET /api/v1/admin/outlets — list all outlets (Admin only)
 */
@RestController
@RequestMapping("/api/v1/admin/outlets")
@PreAuthorize("hasRole('ADMIN')")
public class OutletController {

    private final OutletRepository outletRepository;

    public OutletController(OutletRepository outletRepository) {
        this.outletRepository = outletRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OutletResponse>>> listOutlets() {
        List<Outlet> outlets = outletRepository.findAll();
        List<OutletResponse> responses = outlets.stream()
                .map(o -> new OutletResponse(o.getId(), o.getName(), o.getAddress(), o.getCity(), o.getState(), o.getPincode(), o.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    public static class OutletResponse {
        private Long id;
        private String name;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String status;

        public OutletResponse() {
        }

        public OutletResponse(Long id, String name, String address, String city, String state, String pincode, String status) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.city = city;
            this.state = state;
            this.pincode = pincode;
            this.status = status;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getPincode() { return pincode; }
        public void setPincode(String pincode) { this.pincode = pincode; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
