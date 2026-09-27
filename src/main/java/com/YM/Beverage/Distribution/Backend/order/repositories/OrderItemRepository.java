package com.YM.Beverage.Distribution.Backend.order.repositories;

import com.YM.Beverage.Distribution.Backend.order.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrderId(UUID orderId);
    boolean existsByProductId(UUID productId);
}
