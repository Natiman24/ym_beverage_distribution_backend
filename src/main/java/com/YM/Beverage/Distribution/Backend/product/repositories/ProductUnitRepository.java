package com.YM.Beverage.Distribution.Backend.product.repositories;

import com.YM.Beverage.Distribution.Backend.product.models.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProductUnitRepository extends JpaRepository<ProductUnit, UUID>, JpaSpecificationExecutor<ProductUnit> {
    Optional<ProductUnit> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
