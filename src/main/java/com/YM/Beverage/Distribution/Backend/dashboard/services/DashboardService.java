package com.YM.Beverage.Distribution.Backend.dashboard.services;

import com.YM.Beverage.Distribution.Backend.dashboard.dtos.DashboardSummaryDTO;
import com.YM.Beverage.Distribution.Backend.dashboard.dtos.LowStockItemDTO;
import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import com.YM.Beverage.Distribution.Backend.order.models.Order;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderRepository;
import com.YM.Beverage.Distribution.Backend.order.specification.OrderSpecification;
import com.YM.Beverage.Distribution.Backend.product.repositories.ProductRepository;
import com.YM.Beverage.Distribution.Backend.store.enums.TransactionType;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreTransactionRepository;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StoreTransactionRepository transactionRepository;

    public ApiResponse getSummary() {
        // Order counts
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.SUBMITTED)
                + orderRepository.countByStatus(OrderStatus.APPROVED);
        long processingOrders = orderRepository.countByStatus(OrderStatus.PROCESSING)
                + orderRepository.countByStatus(OrderStatus.DISPATCHED);
        long returnedOrders = orderRepository.countByStatus(OrderStatus.RETURNED);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        // Delivered today
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        OrderSpecification deliveredTodaySpec = new OrderSpecification(
                null, List.of(OrderStatus.DELIVERED), startOfDay, endOfDay);
        long deliveredToday = orderRepository.count(deliveredTodaySpec);

        // Finance
        BigDecimal cashSales = transactionRepository.sumAmountByType(TransactionType.CASH_PAYMENT);
        BigDecimal creditSales = transactionRepository.sumAmountByType(TransactionType.CREDIT);
        BigDecimal creditRepayments = transactionRepository.sumAmountByType(TransactionType.CREDIT_REPAYMENT);

        BigDecimal cashRefunds = transactionRepository.sumAmountByTypeAndOrderPaymentMethod(
                        TransactionType.REFUND, PaymentMethod.CASH)
                .add(transactionRepository.sumOrderlessAmountByType(TransactionType.REFUND));
        BigDecimal creditRefunds = transactionRepository.sumAmountByTypeAndOrderPaymentMethod(
                TransactionType.REFUND, PaymentMethod.CREDIT);

        BigDecimal collectedRevenue = cashSales.add(creditRepayments).subtract(cashRefunds);
        BigDecimal uncollectedRevenue = creditSales.subtract(creditRepayments).subtract(creditRefunds);
        BigDecimal totalRevenue = collectedRevenue.add(uncollectedRevenue);

        // Stock
        long totalProducts = productRepository.countByActiveTrue();
        List<LowStockItemDTO> lowStockItems = productRepository.findLowStockProducts()
                .stream()
                .map(p -> LowStockItemDTO.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .quantity(p.getQuantity())
                        .build())
                .toList();

        DashboardSummaryDTO summary = DashboardSummaryDTO.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .processingOrders(processingOrders)
                .deliveredToday(deliveredToday)
                .returnedOrders(returnedOrders)
                .cancelledOrders(cancelledOrders)
                .totalOutstandingCredit(uncollectedRevenue)
                .totalRevenueAllTime(totalRevenue)
                .collectedRevenueAllTime(collectedRevenue)
                .uncollectedRevenueAllTime(uncollectedRevenue)
                .totalProducts(totalProducts)
                .lowStockProducts(lowStockItems.size())
                .lowStockItems(lowStockItems)
                .build();

        return new ApiResponse("", HttpStatus.OK, Map.of("summary", summary));
    }
}
