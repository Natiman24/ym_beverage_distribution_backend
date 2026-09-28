package com.YM.Beverage.Distribution.Backend.order.services;

import com.YM.Beverage.Distribution.Backend.configs.security.DataScopeService;
import com.YM.Beverage.Distribution.Backend.driver.repositories.DriverRepository;
import com.YM.Beverage.Distribution.Backend.order.dtos.CreateOrderDTO;
import com.YM.Beverage.Distribution.Backend.order.dtos.OrderItemRequestDTO;
import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentStatus;
import com.YM.Beverage.Distribution.Backend.order.models.Order;
import com.YM.Beverage.Distribution.Backend.order.models.OrderItem;
import com.YM.Beverage.Distribution.Backend.order.models.OrderStatusHistory;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderRepository;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderStatusHistoryRepository;
import com.YM.Beverage.Distribution.Backend.product.enums.HistoryMode;
import com.YM.Beverage.Distribution.Backend.product.models.Product;
import com.YM.Beverage.Distribution.Backend.product.models.ProductHistory;
import com.YM.Beverage.Distribution.Backend.product.repositories.ProductHistoryRepository;
import com.YM.Beverage.Distribution.Backend.product.repositories.ProductRepository;
import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreRepository;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreTransactionRepository;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private ProductRepository productRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private StoreTransactionRepository storeTransactionRepository;
    @Mock private ProductHistoryRepository productHistoryRepository;
    @Mock private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private DataScopeService dataScopeService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, storeRepository, productRepository,
                driverRepository, storeTransactionRepository, productHistoryRepository,
                orderStatusHistoryRepository, userRepository, dataScopeService);
    }

    @Test
    void dispatchDeductsStockAndRecordsSaleHistory() {
        Product product = product("Water 500ml", 10);
        Order order = order(OrderStatus.PROCESSING, product, 4);
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(productRepository.findAllActiveByIdForUpdate(any())).thenReturn(List.of(product));

        orderService.dispatchOrder(order.getId());

        assertEquals(6, product.getQuantity());
        assertEquals(OrderStatus.DISPATCHED, order.getStatus());

        ArgumentCaptor<ProductHistory> history = ArgumentCaptor.forClass(ProductHistory.class);
        verify(productHistoryRepository).save(history.capture());
        assertEquals(HistoryMode.SALE, history.getValue().getHistoryMode());
        assertEquals(-4, history.getValue().getQuantityChanged());
    }

    @Test
    void createOrderUsesOverridesAndVerifiesSubmittedTotal() {
        Store store = Store.builder().id(UUID.randomUUID()).name("Test Store").active(true).build();
        Product defaultPrice = product("Water 500ml", 10);
        Product overriddenPrice = product("Water 1L", 10);
        when(storeRepository.findByIdAndActiveTrue(store.getId())).thenReturn(Optional.of(store));
        UUID requesterId = authorizeRequesterFor(store);
        when(productRepository.findByIdAndActiveTrue(defaultPrice.getId())).thenReturn(Optional.of(defaultPrice));
        when(productRepository.findByIdAndActiveTrue(overriddenPrice.getId())).thenReturn(Optional.of(overriddenPrice));

        CreateOrderDTO dto = CreateOrderDTO.builder()
                .storeId(store.getId())
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(BigDecimal.valueOf(34))
                .items(List.of(
                        OrderItemRequestDTO.builder().productId(defaultPrice.getId()).quantity(2).build(),
                        OrderItemRequestDTO.builder().productId(overriddenPrice.getId()).quantity(2)
                                .unitPrice(BigDecimal.valueOf(7)).build()))
                .build();

        orderService.createOrder(dto, requesterId);

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(savedOrder.capture());
        assertEquals(BigDecimal.valueOf(34), savedOrder.getValue().getTotalAmount());
        assertEquals(BigDecimal.TEN, savedOrder.getValue().getItems().get(0).getUnitPrice());
        assertEquals(BigDecimal.valueOf(7), savedOrder.getValue().getItems().get(1).getUnitPrice());
        assertEquals(PaymentStatus.UNPAID, savedOrder.getValue().getPaymentStatus());
    }

    @Test
    void createOrderRejectsIncorrectSubmittedTotal() {
        Store store = Store.builder().id(UUID.randomUUID()).name("Test Store").active(true).build();
        Product product = product("Water 500ml", 10);
        when(storeRepository.findByIdAndActiveTrue(store.getId())).thenReturn(Optional.of(store));
        UUID requesterId = authorizeRequesterFor(store);
        when(productRepository.findByIdAndActiveTrue(product.getId())).thenReturn(Optional.of(product));

        CreateOrderDTO dto = CreateOrderDTO.builder()
                .storeId(store.getId())
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(BigDecimal.valueOf(21))
                .items(List.of(OrderItemRequestDTO.builder()
                        .productId(product.getId()).quantity(2).build()))
                .build();

        assertThrows(CustomException.class, () -> orderService.createOrder(dto, requesterId));
        verify(orderRepository, never()).save(any());
        verify(orderStatusHistoryRepository, never()).save(any());
    }

    @Test
    void creditOrderRequiresDueDate() {
        Store store = Store.builder().id(UUID.randomUUID()).name("Test Store").active(true).build();
        when(storeRepository.findByIdAndActiveTrue(store.getId())).thenReturn(Optional.of(store));
        UUID requesterId = authorizeRequesterFor(store);

        CreateOrderDTO dto = CreateOrderDTO.builder()
                .storeId(store.getId())
                .paymentMethod(PaymentMethod.CREDIT)
                .totalPrice(BigDecimal.TEN)
                .items(List.of(OrderItemRequestDTO.builder()
                        .productId(UUID.randomUUID()).quantity(1).build()))
                .build();

        assertThrows(CustomException.class, () -> orderService.createOrder(dto, requesterId));
        verify(productRepository, never()).findByIdAndActiveTrue(any());
    }

    @Test
    void dispatchFailsWithoutChangingOrderWhenStockIsInsufficient() {
        Product product = product("Water 500ml", 3);
        Order order = order(OrderStatus.PROCESSING, product, 4);
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(productRepository.findAllActiveByIdForUpdate(any())).thenReturn(List.of(product));

        assertThrows(CustomException.class, () -> orderService.dispatchOrder(order.getId()));

        assertEquals(3, product.getQuantity());
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        verify(productRepository, never()).saveAll(any());
        verify(storeTransactionRepository, never()).save(any());
    }

    @Test
    void deliveryDoesNotDeductStockAgain() {
        Product product = product("Water 500ml", 6);
        Order order = order(OrderStatus.DISPATCHED, product, 4);
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));

        orderService.deliverOrder(order.getId());

        assertEquals(6, product.getQuantity());
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
        verify(productRepository, never()).findAllActiveByIdForUpdate(any());
        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void returnRestoresStockAndRecordsReturnHistory() {
        Product product = product("Water 500ml", 6);
        Order order = order(OrderStatus.DELIVERED, product, 4);
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(productRepository.findAllByIdForUpdateIncludingInactive(any())).thenReturn(List.of(product));

        orderService.returnOrder(order.getId(), "Damaged bottles");

        assertEquals(10, product.getQuantity());
        assertEquals(OrderStatus.RETURNED, order.getStatus());
        assertEquals(PaymentStatus.REFUNDED, order.getPaymentStatus());

        ArgumentCaptor<ProductHistory> history = ArgumentCaptor.forClass(ProductHistory.class);
        verify(productHistoryRepository).save(history.capture());
        assertEquals(HistoryMode.RETURN, history.getValue().getHistoryMode());
        assertEquals(4, history.getValue().getQuantityChanged());

        ArgumentCaptor<OrderStatusHistory> statusHistory = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(orderStatusHistoryRepository).save(statusHistory.capture());
        assertEquals("Damaged bottles", statusHistory.getValue().getReason());
    }

    @Test
    void returningUndeliveredDispatchDoesNotCreateRefund() {
        Product product = product("Water 500ml", 6);
        Order order = order(OrderStatus.DISPATCHED, product, 4);
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(productRepository.findAllByIdForUpdateIncludingInactive(any())).thenReturn(List.of(product));

        orderService.returnOrder(order.getId(), "Customer unavailable");

        assertEquals(10, product.getQuantity());
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
        verify(storeTransactionRepository, never()).save(any());
    }

    private Product product(String name, int quantity) {
        return Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .quantity(quantity)
                .sellingPrice(BigDecimal.TEN)
                .build();
    }

    private UUID authorizeRequesterFor(Store store) {
        UUID requesterId = UUID.randomUUID();
        User requester = User.builder()
                .id(requesterId)
                .store(store)
                .roles(new HashSet<>())
                .permissions(new HashSet<>())
                .build();
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        return requesterId;
    }

    private Order order(OrderStatus status, Product product, int quantity) {
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .store(Store.builder().id(UUID.randomUUID()).name("Test Store").build())
                .status(status)
                .paymentMethod(PaymentMethod.CREDIT)
                .orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.valueOf(quantity * 10L))
                .build();
        order.setItems(List.of(OrderItem.builder()
                .id(UUID.randomUUID())
                .order(order)
                .product(product)
                .quantity(quantity)
                .unitPrice(BigDecimal.TEN)
                .totalPrice(BigDecimal.valueOf(quantity * 10L))
                .build()));
        return order;
    }
}
