package com.YM.Beverage.Distribution.Backend.dashboard.controllers;

import com.YM.Beverage.Distribution.Backend.dashboard.services.DashboardService;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse> getSummary() {
        ApiResponse response = dashboardService.getSummary();
        return new ResponseEntity<>(response, response.getStatusCode());
    }
}
