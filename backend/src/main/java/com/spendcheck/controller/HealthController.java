package com.spendcheck.controller;

import com.spendcheck.dto.ApiResponse;
import com.spendcheck.dto.HealthResponse;
import com.spendcheck.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<HealthResponse>> getHealth() {
        HealthResponse health = healthService.checkHealth();
        return ResponseEntity.ok(ApiResponse.ok("SpendCheck API is running", health));
    }
}
