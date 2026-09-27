package com.YM.Beverage.Distribution.Backend.dashboard.services;

import com.YM.Beverage.Distribution.Backend.dashboard.dtos.DashboardSummaryDTO;
import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderRepository;
import com.YM.Beverage.Distribution.Backend.product.repositories.ProductRepository;
import com.YM.Beverage.Distribution.Backend.store.enums.TransactionType;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreTransactionRepository;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private StoreTransactionRepository transactionRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(orderRepository, productRepository, transactionRepository);
        when(productRepository.findLowStockProducts()).thenReturn(List.of());
    }

    @Test
    void separatesCollectedAndUncollectedRevenueAndAccountsForReturns() {
        when(transactionRepository.sumAmountByType(TransactionType.CASH_PAYMENT))
                .thenReturn(new BigDecimal("1000.00"));
        when(transactionRepository.sumAmountByType(TransactionType.CREDIT))
                .thenReturn(new BigDecimal("800.00"));
        when(transactionRepository.sumAmountByType(TransactionType.CREDIT_REPAYMENT))
                .thenReturn(new BigDecimal("300.00"));
        when(transactionRepository.sumAmountByTypeAndOrderPaymentMethod(
                TransactionType.REFUND, PaymentMethod.CASH)).thenReturn(new BigDecimal("100.00"));
        when(transactionRepository.sumOrderlessAmountByType(TransactionType.REFUND))
                .thenReturn(new BigDecimal("50.00"));
        when(transactionRepository.sumAmountByTypeAndOrderPaymentMethod(
                TransactionType.REFUND, PaymentMethod.CREDIT)).thenReturn(new BigDecimal("200.00"));

        ApiResponse response = dashboardService.getSummary();
        DashboardSummaryDTO summary = (DashboardSummaryDTO) response.getDetails().get("summary");

        assertEquals(new BigDecimal("1150.00"), summary.getCollectedRevenueAllTime());
        assertEquals(new BigDecimal("300.00"), summary.getUncollectedRevenueAllTime());
        assertEquals(new BigDecimal("1450.00"), summary.getTotalRevenueAllTime());
        assertEquals(summary.getUncollectedRevenueAllTime(), summary.getTotalOutstandingCredit());
        assertEquals(summary.getTotalRevenueAllTime(),
                summary.getCollectedRevenueAllTime().add(summary.getUncollectedRevenueAllTime()));
    }
}
