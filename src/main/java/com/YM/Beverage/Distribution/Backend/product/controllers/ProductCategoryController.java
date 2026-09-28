package com.YM.Beverage.Distribution.Backend.product.controllers;

import com.YM.Beverage.Distribution.Backend.product.dtos.CreateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.dtos.UpdateLookupDTO;
import com.YM.Beverage.Distribution.Backend.product.services.ProductCategoryService;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-categories")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('LOOKUP_CREATE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> createCategory(@Valid @RequestBody CreateLookupDTO dto) {
        ApiResponse response = productCategoryService.createCategory(dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LOOKUP_VIEW')")
    public ResponseEntity<ApiResponse> getCategories(
            @RequestParam(value = "search-query", required = false) String searchQuery,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize) {
        ApiResponse response = productCategoryService.getCategories(searchQuery, active, page, pageSize);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_VIEW')")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable UUID id) {
        ApiResponse response = productCategoryService.getCategoryById(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_UPDATE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateLookupDTO dto) {
        ApiResponse response = productCategoryService.updateCategory(id, dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_DELETE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable UUID id) {
        ApiResponse response = productCategoryService.deleteCategory(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }
}
