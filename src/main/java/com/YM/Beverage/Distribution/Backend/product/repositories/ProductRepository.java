package com.YM.Beverage.Distribution.Backend.product.repositories;

import com.YM.Beverage.Distribution.Backend.product.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndActiveTrue(UUID id);

    boolean existsByIdAndActiveTrue(UUID id);

    long countByActiveTrue();
    long countByActiveTrueAndQuantity(int quantity);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.quantity > p.thresholdQuantity")
    long countHealthyProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.quantity > 0 AND p.quantity <= p.thresholdQuantity")
    long countLowStockProducts();

    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Product p WHERE p.active = true")
    Long sumActiveProductUnits();

    @Query("SELECT COALESCE(SUM(p.quantity * p.sellingPrice), 0) FROM Product p WHERE p.active = true")
    java.math.BigDecimal sumRetailStockValue();

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.thresholdQuantity AND p.active = true ORDER BY p.quantity ASC")
    List<Product> findLowStockProducts();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.active = true ORDER BY p.id")
    List<Product> findAllActiveByIdForUpdate(@Param("ids") List<UUID> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id")
    List<Product> findAllByIdForUpdateIncludingInactive(@Param("ids") List<UUID> ids);
}
