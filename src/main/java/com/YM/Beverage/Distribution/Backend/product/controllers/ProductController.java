package com.YM.Beverage.Distribution.Backend.product.controllers;

import com.YM.Beverage.Distribution.Backend.product.dtos.AdjustQuantityDTO;
import com.YM.Beverage.Distribution.Backend.product.dtos.CreateProductDTO;
import com.YM.Beverage.Distribution.Backend.product.dtos.UpdateProductDTO;
import com.YM.Beverage.Distribution.Backend.product.services.ProductService;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody CreateProductDTO dto) {
        ApiResponse response = productService.createProduct(dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam(value = "search-query", required = false) String searchQuery,
            @RequestParam(value = "category-ids", required = false) List<UUID> categoryIds,
            @RequestParam(value = "unit-ids", required = false) List<UUID> unitIds,
            @RequestParam(value = "brand-ids", required = false) List<UUID> brandIds,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize) {
        ApiResponse response = productService.getProducts(searchQuery, categoryIds, unitIds, brandIds, active, page, pageSize);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable UUID id) {
        ApiResponse response = productService.getProductById(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable UUID id,
                                                     @Valid @RequestBody UpdateProductDTO dto) {
        ApiResponse response = productService.updateProduct(id, dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable UUID id) {
        ApiResponse response = productService.deleteProduct(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<ApiResponse> adjustQuantity(@PathVariable UUID id,
                                                      @Valid @RequestBody AdjustQuantityDTO dto) {
        ApiResponse response = productService.adjustQuantity(id, dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse> getProductHistory(
            @PathVariable UUID id,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize) {
        ApiResponse response = productService.getProductHistory(id, page, pageSize);
        return new ResponseEntity<>(response, response.getStatusCode());
    }
}
