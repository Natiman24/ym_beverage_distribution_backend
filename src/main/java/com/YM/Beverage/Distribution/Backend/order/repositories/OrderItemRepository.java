package com.YM.Beverage.Distribution.Backend.order.repositories;

import com.YM.Beverage.Distribution.Backend.order.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrderId(UUID orderId);
    boolean existsByProductId(UUID productId);

    @Query(value = "SELECT p.id, p.name, COALESCE(SUM(oi.quantity), 0), COUNT(DISTINCT o.id), COALESCE(SUM(oi.total_price), 0) FROM order_items oi JOIN orders o ON o.id = oi.order_id JOIN products p ON p.id = oi.product_id WHERE o.delivered_at >= :fromDate AND o.delivered_at < :toDate AND o.status = 'DELIVERED' GROUP BY p.id, p.name ORDER BY SUM(oi.quantity) DESC LIMIT 5", nativeQuery = true)
    List<Object[]> topProducts(@Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to);
}
