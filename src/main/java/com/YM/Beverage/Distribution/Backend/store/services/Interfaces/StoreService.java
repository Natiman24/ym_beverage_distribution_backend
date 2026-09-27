package com.YM.Beverage.Distribution.Backend.store.services.Interfaces;

import com.YM.Beverage.Distribution.Backend.store.dtos.CreateStoreDTO;
import com.YM.Beverage.Distribution.Backend.store.dtos.UpdateStoreDTO;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;

import java.util.UUID;

public interface StoreService {
    ApiResponse createStore(CreateStoreDTO createStoreDTO);
    ApiResponse getStores(String searchQuery, Boolean isActive, Integer page, Integer pageSize);
    ApiResponse getStoreById(UUID id);
    ApiResponse editStore(UUID id, UpdateStoreDTO updateStoreDTO);
    ApiResponse deleteStore(UUID id);
    ApiResponse changeActivationStatus(UUID id);
}
