package com.YM.Beverage.Distribution.Backend.dashboard.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardAnalyticsDTO {
    private int periodDays;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDateTime generatedAt;
    private KPI kpis;
    private Inventory inventory;
    private List<TrendPoint> salesTrend;
    private List<Breakdown> orderStatuses;
    private List<Breakdown> paymentStatuses;
    private List<Ranking> topProducts;
    private List<Ranking> topStores;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class KPI {
        private long orders;
        private double ordersChangePercent;
        private long deliveredOrders;
        private double deliveryRatePercent;
        private BigDecimal deliveredRevenue;
        private double revenueChangePercent;
        private BigDecimal deliveredCost;
        private BigDecimal grossProfit;
        private double grossProfitChangePercent;
        private double grossProfitMarginPercent;
        private BigDecimal averageOrderValue;
        private double averageDeliveryHours;
        private long activeStores;
        private long availableDrivers;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Inventory {
        private long activeProducts;
        private long healthyProducts;
        private long lowStockProducts;
        private long outOfStockProducts;
        private long totalUnits;
        private BigDecimal retailStockValue;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TrendPoint {
        private LocalDate date;
        private long orders;
        private BigDecimal revenue;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Breakdown {
        private String label;
        private long count;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Ranking {
        private UUID id;
        private String name;
        private long quantity;
        private long orders;
        private BigDecimal revenue;
    }
}
