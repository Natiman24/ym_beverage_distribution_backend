package com.YM.Beverage.Distribution.Backend.supplier.services.Interfaces;

import com.YM.Beverage.Distribution.Backend.supplier.dtos.CreateSupplierDTO;
import com.YM.Beverage.Distribution.Backend.supplier.dtos.UpdateSupplierDTO;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;

import java.util.UUID;

public interface SupplierService {
    ApiResponse createSupplier(CreateSupplierDTO createSupplierDTO);
    ApiResponse getSuppliers(Integer page, Integer pageSize, String searchQuery, Boolean active);
    ApiResponse getSupplierById(UUID id);
    ApiResponse editSupplier(UUID id, UpdateSupplierDTO updateSupplierDTO);
    ApiResponse deleteSupplier(UUID id);
}
