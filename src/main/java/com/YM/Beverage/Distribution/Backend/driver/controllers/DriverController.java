package com.YM.Beverage.Distribution.Backend.driver.controllers;

import com.YM.Beverage.Distribution.Backend.configs.security.JwtUtil;
import com.YM.Beverage.Distribution.Backend.driver.dtos.CreateDriverDTO;
import com.YM.Beverage.Distribution.Backend.driver.dtos.UpdateDriverDTO;
import com.YM.Beverage.Distribution.Backend.driver.services.DriverService;
import com.YM.Beverage.Distribution.Backend.order.services.OrderService;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/drivers")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class DriverController {

    private final DriverService driverService;
    private final OrderService orderService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest httpServletRequest;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('DRIVER_VIEW_PROFILE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> getMyProfile() {
        ApiResponse response = driverService.getMyProfile(jwtUtil.getUserId(jwtUtil.resolveToken(httpServletRequest)));
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DRIVER_CREATE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> createDriver(@Valid @RequestBody CreateDriverDTO dto) {
        ApiResponse response = driverService.createDriver(dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DRIVER_VIEW') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> getDrivers(
            @RequestParam(value = "search-query", required = false) String searchQuery,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "is-deactivated", required = false) Boolean isDeactivated,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize) {
        ApiResponse response = driverService.getDrivers(searchQuery, active, isDeactivated, page, pageSize);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DRIVER_VIEW') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> getDriverById(@PathVariable UUID id) {
        ApiResponse response = driverService.getDriverById(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('DRIVER_UPDATE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> updateDriver(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateDriverDTO dto) {
        ApiResponse response = driverService.updateDriver(id, dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('DRIVER_TOGGLE_ACTIVE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> toggleActive(@PathVariable UUID id) {
        ApiResponse response = driverService.toggleActiveStatus(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DRIVER_DELETE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> deleteDriver(@PathVariable UUID id) {
        ApiResponse response = driverService.deleteDriver(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/{id}/orders")
    @PreAuthorize("hasAuthority('DRIVER_VIEW_ORDERS') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> getDriverOrders(@PathVariable UUID id) {
        ApiResponse response = orderService.getDriverOrders(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }
}
