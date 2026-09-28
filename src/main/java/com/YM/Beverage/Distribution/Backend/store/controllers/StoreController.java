package com.YM.Beverage.Distribution.Backend.store.controllers;

import com.YM.Beverage.Distribution.Backend.store.dtos.CreateStoreDTO;
import com.YM.Beverage.Distribution.Backend.store.dtos.UpdateStoreDTO;
import com.YM.Beverage.Distribution.Backend.store.services.Interfaces.StoreService;
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
@RequestMapping("/api/store")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class StoreController {
    private final StoreService storeService;

    @PostMapping
    @PreAuthorize("hasAuthority('STORE_CREATE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> createStore(@Valid @RequestBody CreateStoreDTO createStoreDTO) {
        ApiResponse apiResponse = storeService.createStore(createStoreDTO);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STORE_VIEW')")
    public ResponseEntity<ApiResponse> getStores(
            @RequestParam(value = "search-query", required = false) String searchQuery,
            @RequestParam(value = "is-active", required = false) Boolean isActive,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize) {
        ApiResponse apiResponse = storeService.getStores(searchQuery, isActive, page, pageSize);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_VIEW')")
    public ResponseEntity<ApiResponse> getStoreById(@PathVariable UUID id) {
        ApiResponse apiResponse = storeService.getStoreById(id);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_UPDATE')")
    public ResponseEntity<ApiResponse> editStore(@PathVariable UUID id, @Valid @RequestBody UpdateStoreDTO updateStoreDTO) {
        ApiResponse apiResponse = storeService.editStore(id, updateStoreDTO);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_DELETE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> deleteStore(@PathVariable UUID id) {
        ApiResponse apiResponse = storeService.deleteStore(id);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }

    @PatchMapping("/{id}/activation")
    @PreAuthorize("hasAuthority('STORE_TOGGLE_ACTIVE') and @dataScope.isCompanyUser(authentication)")
    public ResponseEntity<ApiResponse> changeActivationStatus(@PathVariable UUID id) {
        ApiResponse apiResponse = storeService.changeActivationStatus(id);
        return new ResponseEntity<>(apiResponse, apiResponse.getStatusCode());
    }
}
