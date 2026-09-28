package com.YM.Beverage.Distribution.Backend.order.services;

import com.YM.Beverage.Distribution.Backend.configs.security.DataScopeService;
import com.YM.Beverage.Distribution.Backend.driver.repositories.DriverRepository;
import com.YM.Beverage.Distribution.Backend.order.dtos.CreateOrderDTO;
import com.YM.Beverage.Distribution.Backend.order.dtos.DispatchOrderDTO;
import com.YM.Beverage.Distribution.Backend.order.dtos.OrderItemRequestDTO;
import com.YM.Beverage.Distribution.Backend.order.dtos.OrderSingleResponseDTO;
import com.YM.Beverage.Distribution.Backend.order.dtos.UpdateOrderDTO;
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
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

        orderService.dispatchOrder(order.getId(), dispatchDetails());

        assertEquals(6, product.getQuantity());
        assertEquals(OrderStatus.DISPATCHED, order.getStatus());
        assertEquals(dispatchDetails().getExpectedDeliveryDate().toLocalDate(),
                order.getExpectedDeliveryDate().toLocalDate());
        assertEquals(dispatchDetails().getPaymentDueDate(), order.getPaymentDueDate());

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
        assertEquals(OrderStatus.SUBMITTED, savedOrder.getValue().getStatus());
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
    void storeCreditOrderDefersDueDateUntilDispatch() {
        Store store = Store.builder().id(UUID.randomUUID()).name("Test Store").active(true).build();
        Product product = product("Water 500ml", 10);
        when(storeRepository.findByIdAndActiveTrue(store.getId())).thenReturn(Optional.of(store));
        UUID requesterId = authorizeRequesterFor(store);
        when(productRepository.findByIdAndActiveTrue(product.getId())).thenReturn(Optional.of(product));

        CreateOrderDTO dto = CreateOrderDTO.builder()
                .storeId(store.getId())
                .paymentMethod(PaymentMethod.CREDIT)
                .totalPrice(BigDecimal.TEN)
                .items(List.of(OrderItemRequestDTO.builder()
                        .productId(product.getId()).quantity(1).build()))
                .build();

        orderService.createOrder(dto, requesterId);

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(savedOrder.capture());
        assertEquals(OrderStatus.SUBMITTED, savedOrder.getValue().getStatus());
        assertNull(savedOrder.getValue().getPaymentDueDate());
    }

    @Test
    void creatorCanChooseToSaveOrderAsDraft() {
        Store store = Store.builder().id(UUID.randomUUID()).name("Test Store").active(true).build();
        Product product = product("Water 500ml", 10);
        when(storeRepository.findByIdAndActiveTrue(store.getId())).thenReturn(Optional.of(store));
        UUID requesterId = authorizeRequesterFor(store);
        when(productRepository.findByIdAndActiveTrue(product.getId())).thenReturn(Optional.of(product));

        CreateOrderDTO dto = CreateOrderDTO.builder()
                .draft(true)
                .storeId(store.getId())
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(BigDecimal.TEN)
                .items(List.of(OrderItemRequestDTO.builder()
                        .productId(product.getId()).quantity(1).build()))
                .build();

        orderService.createOrder(dto, requesterId);

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(savedOrder.capture());
        assertEquals(OrderStatus.DRAFT, savedOrder.getValue().getStatus());
    }

    @Test
    void draftContentCanBeReplacedBeforeSubmission() {
        Store store = Store.builder().id(UUID.randomUUID()).name("Test Store").build();
        Product oldProduct = product("Old Water", 10);
        Product newProduct = product("New Water", 10);
        Order draft = order(OrderStatus.DRAFT, oldProduct, 1);
        draft.setStore(store);
        draft.setItems(new ArrayList<>(draft.getItems()));
        when(orderRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(dataScopeService.currentStoreId()).thenReturn(store.getId());
        when(dataScopeService.isCurrentUserCompanyUser()).thenReturn(false);
        when(productRepository.findByIdAndActiveTrue(newProduct.getId())).thenReturn(Optional.of(newProduct));

        UpdateOrderDTO dto = UpdateOrderDTO.builder()
                .paymentMethod(PaymentMethod.CREDIT)
                .deliveryAddress("New delivery address")
                .notes("Updated notes")
                .totalPrice(BigDecimal.valueOf(30))
                .items(List.of(OrderItemRequestDTO.builder()
                        .productId(newProduct.getId()).quantity(3).build()))
                .build();

        orderService.updateOrder(draft.getId(), dto);

        assertEquals(PaymentMethod.CREDIT, draft.getPaymentMethod());
        assertEquals("New delivery address", draft.getDeliveryAddress());
        assertEquals("Updated notes", draft.getNotes());
        assertEquals(BigDecimal.valueOf(30), draft.getTotalAmount());
        assertEquals(newProduct.getId(), draft.getItems().get(0).getProduct().getId());
        assertEquals(3, draft.getItems().get(0).getQuantity());
    }

    @Test
    void companyUserCannotReadAStoreCreatedDraft() {
        Product product = product("Water 500ml", 10);
        Order draft = order(OrderStatus.DRAFT, product, 1);
        draft.setCreatedBy(User.builder().id(UUID.randomUUID()).store(draft.getStore()).build());
        when(orderRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(dataScopeService.currentStoreId()).thenReturn(null);
        when(dataScopeService.currentUserId()).thenReturn(UUID.randomUUID());

        assertThrows(CustomException.class, () -> orderService.getOrderById(draft.getId()));
    }

    @Test
    void storeOrderResponsesHideCompanySchedulingFields() {
        Product product = product("Water 500ml", 10);
        Order order = order(OrderStatus.SUBMITTED, product, 1);
        order.setExpectedDeliveryDate(LocalDateTime.now().plusDays(1));
        order.setPaymentDueDate(LocalDate.now().plusDays(14));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(dataScopeService.currentStoreId()).thenReturn(order.getStore().getId());
        when(dataScopeService.isCurrentUserCompanyUser()).thenReturn(false);

        OrderSingleResponseDTO response = (OrderSingleResponseDTO) orderService
                .getOrderById(order.getId()).getDetails().get("order");

        assertNull(response.getExpectedDeliveryDate());
        assertNull(response.getPaymentDueDate());
    }

    @Test
    void dispatchFailsWithoutChangingOrderWhenStockIsInsufficient() {
        Product product = product("Water 500ml", 3);
        Order order = order(OrderStatus.PROCESSING, product, 4);
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(productRepository.findAllActiveByIdForUpdate(any())).thenReturn(List.of(product));

        assertThrows(CustomException.class, () -> orderService.dispatchOrder(order.getId(), dispatchDetails()));

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

    private DispatchOrderDTO dispatchDetails() {
        return DispatchOrderDTO.builder()
                .expectedDeliveryDate(LocalDateTime.now().plusDays(1))
                .paymentDueDate(LocalDate.now().plusDays(14))
                .build();
    }
}
