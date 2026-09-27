package com.YM.Beverage.Distribution.Backend.supplier.controllers;

import com.YM.Beverage.Distribution.Backend.configs.security.JwtUtil;
import com.YM.Beverage.Distribution.Backend.supplier.dtos.CreateSupplierDTO;
import com.YM.Beverage.Distribution.Backend.supplier.dtos.UpdateSupplierDTO;
import com.YM.Beverage.Distribution.Backend.supplier.services.Interfaces.SupplierService;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supplier")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class SupplierController {
    private final SupplierService supplierService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest httpServletRequest;

    @PostMapping()
    public ResponseEntity<ApiResponse> createSupplier(@Valid @RequestBody CreateSupplierDTO createSupplierDTO) {
        ApiResponse apiResponse = supplierService.createSupplier(createSupplierDTO);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getSuppliers(
            @RequestParam(value = "search-query", required = false) String searchQuery,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize) {
        ApiResponse apiResponse = supplierService.getSuppliers(page, pageSize, searchQuery);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getSupplierById(@PathVariable UUID id) {
        ApiResponse apiResponse = supplierService.getSupplierById(id);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> editSupplier(@PathVariable UUID id, @Valid @RequestBody UpdateSupplierDTO updateSupplierDTO) {
        ApiResponse apiResponse = supplierService.editSupplier(id, updateSupplierDTO);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteSupplier(@PathVariable UUID id) {
        ApiResponse apiResponse = supplierService.deleteSupplier(id);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

}
