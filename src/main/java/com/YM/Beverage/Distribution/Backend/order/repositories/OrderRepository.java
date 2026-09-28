package com.YM.Beverage.Distribution.Backend.order.repositories;

import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.order.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    List<Order> findByDriverIdOrderByOrderDateDesc(UUID driverId);
    long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :from AND o.orderDate < :to AND o.status <> com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus.CANCELLED")
    long countReportableOrders(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.deliveredAt >= :from AND o.deliveredAt < :to AND o.status = com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus.DELIVERED")
    long countDeliveredOrders(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.deliveredAt >= :from AND o.deliveredAt < :to AND o.status = com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus.DELIVERED")
    BigDecimal sumDeliveredRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (delivered_at - order_date)) / 3600.0), 0) FROM orders WHERE delivered_at >= :fromDate AND delivered_at < :toDate AND status = 'DELIVERED'", nativeQuery = true)
    Number averageDeliveryHours(@Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to);

    @Query(value = "SELECT CAST(delivered_at AS DATE), COUNT(*), COALESCE(SUM(total_amount), 0) FROM orders WHERE delivered_at >= :fromDate AND delivered_at < :toDate AND status = 'DELIVERED' GROUP BY CAST(delivered_at AS DATE) ORDER BY CAST(delivered_at AS DATE)", nativeQuery = true)
    List<Object[]> dailyDeliveredSales(@Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to);

    @Query(value = "SELECT status, COUNT(*) FROM orders WHERE order_date >= :fromDate AND order_date < :toDate GROUP BY status", nativeQuery = true)
    List<Object[]> statusBreakdown(@Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to);

    @Query(value = "SELECT payment_status, COUNT(*) FROM orders WHERE order_date >= :fromDate AND order_date < :toDate GROUP BY payment_status", nativeQuery = true)
    List<Object[]> paymentBreakdown(@Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to);

    @Query(value = "SELECT s.id, s.name, COUNT(o.id), COALESCE(SUM(o.total_amount), 0) FROM orders o JOIN stores s ON s.id = o.store_id WHERE o.delivered_at >= :fromDate AND o.delivered_at < :toDate AND o.status = 'DELIVERED' GROUP BY s.id, s.name ORDER BY SUM(o.total_amount) DESC LIMIT 5", nativeQuery = true)
    List<Object[]> topStores(@Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") UUID id);
}
