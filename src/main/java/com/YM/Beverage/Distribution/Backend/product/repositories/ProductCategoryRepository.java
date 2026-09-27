package com.YM.Beverage.Distribution.Backend.product.repositories;

import com.YM.Beverage.Distribution.Backend.product.models.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID>, JpaSpecificationExecutor<ProductCategory> {
    Optional<ProductCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
