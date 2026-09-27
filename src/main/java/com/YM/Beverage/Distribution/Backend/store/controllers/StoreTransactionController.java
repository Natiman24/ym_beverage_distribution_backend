package com.YM.Beverage.Distribution.Backend.store.controllers;

import com.YM.Beverage.Distribution.Backend.store.dtos.CreateStoreTransactionDTO;
import com.YM.Beverage.Distribution.Backend.store.services.Interfaces.StoreTransactionService;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/transactions")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class StoreTransactionController {

    private final StoreTransactionService transactionService;

    @PostMapping
    public ResponseEntity<ApiResponse> createTransaction(
            @PathVariable UUID storeId,
            @Valid @RequestBody CreateStoreTransactionDTO dto) {
        ApiResponse response = transactionService.createTransaction(storeId, dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getTransactions(
            @PathVariable UUID storeId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize) {
        ApiResponse response = transactionService.getTransactions(storeId, page, pageSize);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse> getStoreBalance(@PathVariable UUID storeId) {
        ApiResponse response = transactionService.getStoreBalance(storeId);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/orders/{orderId}/balance")
    public ResponseEntity<ApiResponse> getOrderBalance(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId) {
        ApiResponse response = transactionService.getOrderBalance(storeId, orderId);
        return new ResponseEntity<>(response, response.getStatusCode());
    }
}
