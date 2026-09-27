package com.YM.Beverage.Distribution.Backend.store.services.Interfaces;

import com.YM.Beverage.Distribution.Backend.store.dtos.CreateStoreTransactionDTO;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;

import java.util.UUID;

public interface StoreTransactionService {
    ApiResponse createTransaction(UUID storeId, CreateStoreTransactionDTO dto);
    ApiResponse getTransactions(UUID storeId, Integer page, Integer pageSize);
    ApiResponse getStoreBalance(UUID storeId);
    ApiResponse getOrderBalance(UUID storeId, UUID orderId);
}
