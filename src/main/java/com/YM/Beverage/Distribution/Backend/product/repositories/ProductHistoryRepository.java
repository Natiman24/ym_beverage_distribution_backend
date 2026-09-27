package com.YM.Beverage.Distribution.Backend.product.repositories;

import com.YM.Beverage.Distribution.Backend.product.models.ProductHistory;
import com.YM.Beverage.Distribution.Backend.product.enums.HistoryMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductHistoryRepository extends JpaRepository<ProductHistory, UUID> {
    List<ProductHistory> findByProductIdOrderByCreatedAtDesc(UUID productId);
    Page<ProductHistory> findByProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);
    boolean existsByProductIdAndHistoryModeIn(UUID productId, List<HistoryMode> historyModes);
    void deleteByProductId(UUID productId);
}
