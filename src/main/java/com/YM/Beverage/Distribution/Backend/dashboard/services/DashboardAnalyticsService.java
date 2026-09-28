package com.YM.Beverage.Distribution.Backend.dashboard.services;

import com.YM.Beverage.Distribution.Backend.dashboard.dtos.DashboardAnalyticsDTO;
import com.YM.Beverage.Distribution.Backend.driver.repositories.DriverRepository;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderItemRepository;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderRepository;
import com.YM.Beverage.Distribution.Backend.product.repositories.ProductRepository;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreRepository;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final DriverRepository driverRepository;

    public ApiResponse getAnalytics(int days) {
        int safeDays = Math.max(7, Math.min(days, 365));
        LocalDate today = LocalDate.now();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        LocalDateTime from = today.minusDays(safeDays - 1L).atStartOfDay();
        LocalDateTime previousFrom = from.minusDays(safeDays);

        long orders = orderRepository.countReportableOrders(from, to);
        long previousOrders = orderRepository.countReportableOrders(previousFrom, from);
        long delivered = orderRepository.countDeliveredOrders(from, to);
        BigDecimal revenue = orderRepository.sumDeliveredRevenue(from, to);
        BigDecimal previousRevenue = orderRepository.sumDeliveredRevenue(previousFrom, from);
        double averageDeliveryHours = number(orderRepository.averageDeliveryHours(from, to)).doubleValue();

        Map<LocalDate, DashboardAnalyticsDTO.TrendPoint> points = new LinkedHashMap<>();
        for (int i = 0; i < safeDays; i++) {
            LocalDate date = from.toLocalDate().plusDays(i);
            points.put(date, DashboardAnalyticsDTO.TrendPoint.builder()
                    .date(date).orders(0).revenue(BigDecimal.ZERO).build());
        }
        for (Object[] row : orderRepository.dailyDeliveredSales(from, to)) {
            LocalDate date = row[0] instanceof Date sqlDate ? sqlDate.toLocalDate() : LocalDate.parse(row[0].toString());
            points.put(date, DashboardAnalyticsDTO.TrendPoint.builder()
                    .date(date).orders(number(row[1]).longValue()).revenue(decimal(row[2])).build());
        }

        long activeProducts = productRepository.countByActiveTrue();
        DashboardAnalyticsDTO analytics = DashboardAnalyticsDTO.builder()
                .periodDays(safeDays).periodStart(from.toLocalDate()).periodEnd(today).generatedAt(LocalDateTime.now())
                .kpis(DashboardAnalyticsDTO.KPI.builder()
                        .orders(orders).ordersChangePercent(change(orders, previousOrders))
                        .deliveredOrders(delivered).deliveryRatePercent(percent(delivered, orders))
                        .deliveredRevenue(revenue).revenueChangePercent(change(revenue, previousRevenue))
                        .averageOrderValue(delivered == 0 ? BigDecimal.ZERO : revenue.divide(BigDecimal.valueOf(delivered), 2, RoundingMode.HALF_UP))
                        .averageDeliveryHours(Math.round(averageDeliveryHours * 10.0) / 10.0)
                        .activeStores(storeRepository.countByActiveTrue()).availableDrivers(driverRepository.countAvailableDrivers()).build())
                .inventory(DashboardAnalyticsDTO.Inventory.builder()
                        .activeProducts(activeProducts).healthyProducts(productRepository.countHealthyProducts())
                        .lowStockProducts(productRepository.countLowStockProducts())
                        .outOfStockProducts(productRepository.countByActiveTrueAndQuantity(0))
                        .totalUnits(Optional.ofNullable(productRepository.sumActiveProductUnits()).orElse(0L))
                        .retailStockValue(Optional.ofNullable(productRepository.sumRetailStockValue()).orElse(BigDecimal.ZERO)).build())
                .salesTrend(new ArrayList<>(points.values()))
                .orderStatuses(breakdowns(orderRepository.statusBreakdown(from, to)))
                .paymentStatuses(breakdowns(orderRepository.paymentBreakdown(from, to)))
                .topProducts(rankings(orderItemRepository.topProducts(from, to), true))
                .topStores(rankings(orderRepository.topStores(from, to), false)).build();
        return new ApiResponse("", HttpStatus.OK, Map.of("analytics", analytics));
    }

    private List<DashboardAnalyticsDTO.Breakdown> breakdowns(List<Object[]> rows) {
        return rows.stream().map(row -> DashboardAnalyticsDTO.Breakdown.builder()
                .label(row[0].toString()).count(number(row[1]).longValue()).build()).toList();
    }

    private List<DashboardAnalyticsDTO.Ranking> rankings(List<Object[]> rows, boolean product) {
        return rows.stream().map(row -> DashboardAnalyticsDTO.Ranking.builder()
                .id((UUID) row[0]).name(row[1].toString())
                .quantity(product ? number(row[2]).longValue() : 0)
                .orders(product ? number(row[3]).longValue() : number(row[2]).longValue())
                .revenue(decimal(product ? row[4] : row[3])).build()).toList();
    }

    private Number number(Object value) { return value instanceof Number number ? number : 0; }
    private BigDecimal decimal(Object value) { return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString()); }
    private double percent(long part, long total) { return total == 0 ? 0 : Math.round(part * 1000.0 / total) / 10.0; }
    private double change(long current, long previous) { return previous == 0 ? (current == 0 ? 0 : 100) : Math.round((current - previous) * 1000.0 / previous) / 10.0; }
    private double change(BigDecimal current, BigDecimal previous) { return previous.signum() == 0 ? (current.signum() == 0 ? 0 : 100) : current.subtract(previous).multiply(BigDecimal.valueOf(100)).divide(previous, 1, RoundingMode.HALF_UP).doubleValue(); }
}
