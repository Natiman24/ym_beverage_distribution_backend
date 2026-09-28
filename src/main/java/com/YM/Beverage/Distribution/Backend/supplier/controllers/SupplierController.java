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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('SUPPLIER_CREATE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> createSupplier(@Valid @RequestBody CreateSupplierDTO createSupplierDTO) {
        ApiResponse apiResponse = supplierService.createSupplier(createSupplierDTO);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER_VIEW') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> getSuppliers(
            @RequestParam(value = "search-query", required = false) String searchQuery,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize) {
        ApiResponse apiResponse = supplierService.getSuppliers(page, pageSize, searchQuery, active);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_VIEW') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> getSupplierById(@PathVariable UUID id) {
        ApiResponse apiResponse = supplierService.getSupplierById(id);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_UPDATE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> editSupplier(@PathVariable UUID id, @Valid @RequestBody UpdateSupplierDTO updateSupplierDTO) {
        ApiResponse apiResponse = supplierService.editSupplier(id, updateSupplierDTO);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_DELETE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> deleteSupplier(@PathVariable UUID id) {
        ApiResponse apiResponse = supplierService.deleteSupplier(id);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

}
