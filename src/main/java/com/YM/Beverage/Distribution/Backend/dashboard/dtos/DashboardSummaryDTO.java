package com.YM.Beverage.Distribution.Backend.dashboard.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {

    // Orders
    private long totalOrders;
    private long pendingOrders;        // SUBMITTED + APPROVED
    private long processingOrders;     // PROCESSING + DISPATCHED
    private long deliveredToday;
    private long returnedOrders;
    private long cancelledOrders;

    // Finance
    private BigDecimal totalOutstandingCredit;       // same value as uncollectedRevenueAllTime
    private BigDecimal totalRevenueAllTime;           // collected + uncollected, net of returns
    private BigDecimal collectedRevenueAllTime;       // cash sales + credit repayments - cash refunds
    private BigDecimal uncollectedRevenueAllTime;     // credit sales - repayments - credit returns

    // Stock
    private long totalProducts;
    private long lowStockProducts;              // quantity <= lowStockThreshold
    private List<LowStockItemDTO> lowStockItems;
}
