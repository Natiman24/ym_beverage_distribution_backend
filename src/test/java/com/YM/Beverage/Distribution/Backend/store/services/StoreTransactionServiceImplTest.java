package com.YM.Beverage.Distribution.Backend.store.services;

import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentStatus;
import com.YM.Beverage.Distribution.Backend.order.models.Order;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderRepository;
import com.YM.Beverage.Distribution.Backend.store.dtos.CreateStoreTransactionDTO;
import com.YM.Beverage.Distribution.Backend.store.enums.TransactionType;
import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreRepository;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreTransactionRepository;
import com.YM.Beverage.Distribution.Backend.store.services.Implementations.StoreTransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreTransactionServiceImplTest {

    @Mock private StoreTransactionRepository transactionRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private OrderRepository orderRepository;

    @Test
    void repaymentMarksOrderAsPartiallyPaid() {
        Store store = Store.builder().id(UUID.randomUUID()).name("Test Store").active(true).build();
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .store(store)
                .status(OrderStatus.DELIVERED)
                .paymentMethod(PaymentMethod.CREDIT)
                .paymentStatus(PaymentStatus.UNPAID)
                .totalAmount(BigDecimal.valueOf(100))
                .build();
        CreateStoreTransactionDTO dto = CreateStoreTransactionDTO.builder()
                .transactionType(TransactionType.CREDIT_REPAYMENT)
                .amount(BigDecimal.valueOf(40))
                .orderId(order.getId())
                .build();

        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(transactionRepository.sumAmountByOrderIdAndTypes(eq(order.getId()), anyList()))
                .thenReturn(BigDecimal.ZERO, BigDecimal.valueOf(40));

        new StoreTransactionServiceImpl(transactionRepository, storeRepository, orderRepository)
                .createTransaction(store.getId(), dto);

        assertEquals(PaymentStatus.PARTIALLY_PAID, order.getPaymentStatus());
    }
}
