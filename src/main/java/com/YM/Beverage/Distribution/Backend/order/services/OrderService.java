package com.YM.Beverage.Distribution.Backend.order.services;

import com.YM.Beverage.Distribution.Backend.configs.security.DataScopeService;
import com.YM.Beverage.Distribution.Backend.driver.models.Driver;
import com.YM.Beverage.Distribution.Backend.driver.repositories.DriverRepository;
import com.YM.Beverage.Distribution.Backend.order.dtos.*;
import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentStatus;
import com.YM.Beverage.Distribution.Backend.order.models.Order;
import com.YM.Beverage.Distribution.Backend.order.models.OrderItem;
import com.YM.Beverage.Distribution.Backend.order.models.OrderStatusHistory;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderRepository;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderStatusHistoryRepository;
import com.YM.Beverage.Distribution.Backend.order.specification.OrderSpecification;
import com.YM.Beverage.Distribution.Backend.product.models.Product;
import com.YM.Beverage.Distribution.Backend.product.models.ProductHistory;
import com.YM.Beverage.Distribution.Backend.product.enums.HistoryMode;
import com.YM.Beverage.Distribution.Backend.product.repositories.ProductHistoryRepository;
import com.YM.Beverage.Distribution.Backend.product.repositories.ProductRepository;
import com.YM.Beverage.Distribution.Backend.store.enums.TransactionType;
import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.store.models.StoreTransaction;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreRepository;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreTransactionRepository;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final DriverRepository driverRepository;
    private final StoreTransactionRepository storeTransactionRepository;
    private final ProductHistoryRepository productHistoryRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final UserRepository userRepository;
    private final DataScopeService dataScopeService;

    @Transactional
    public ApiResponse createOrder(CreateOrderDTO dto, UUID userId) {
        Store store = storeRepository.findByIdAndActiveTrue(dto.getStoreId())
                .orElseThrow(() -> new DataNotFoundException("Active store not found"));

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Requesting user not found"));

        if (requester.getStore() != null
                && !requester.getStore().getId().equals(store.getId())) {
            throw new CustomException("You are not authorized to create an order for this store", HttpStatus.FORBIDDEN, "");
        }

        boolean companyUser = requester.getStore() == null;
        validateCreateScheduling(dto, companyUser);
        OrderStatus initialStatus = dto.isDraft() ? OrderStatus.DRAFT : OrderStatus.SUBMITTED;

        Order order = Order.builder()
                .store(store)
                .status(initialStatus)
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .paymentDueDate(companyUser && dto.getPaymentMethod() == PaymentMethod.CREDIT
                        ? dto.getPaymentDueDate() : null)
                .orderDate(LocalDateTime.now())
                .expectedDeliveryDate(companyUser ? dto.getExpectedDeliveryDate() : null)
                .deliveryAddress(dto.getDeliveryAddress())
                .notes(dto.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        List<OrderItem> items = buildItems(order, dto.getItems());
        BigDecimal subtotal = calculateTotal(items);
        order.setTotalAmount(subtotal);
        order.setItems(items);
        verifySubmittedTotal(dto.getTotalPrice(), subtotal);

        orderRepository.save(order);
        recordStatusChange(order, null, initialStatus, null);

        String message = dto.isDraft() ? "Draft order saved successfully" : "Order submitted successfully";
        return new ApiResponse(message, HttpStatus.CREATED,
                Map.of("order", order.toSingleResponseDTO(companyUser)));
    }

    public ApiResponse getOrders(UUID storeId, List<OrderStatus> statuses,
                                  LocalDateTime fromDate, LocalDateTime toDate,
                                  UUID driverId, PaymentMethod paymentMethod,
                                  PaymentStatus paymentStatus,
                                  LocalDateTime deliveredFromDate, LocalDateTime deliveredToDate,
                                  UUID createdByUserId, Boolean overdueCredit, String storeSearch,
                                  Integer page, Integer pageSize) {

        UUID requesterStoreId = dataScopeService.currentStoreId();
        boolean companyUser = requesterStoreId == null;
        if (requesterStoreId != null) {
            storeId = requesterStoreId;
            overdueCredit = null;
        }

        UUID visibleCompanyDraftCreatorId = companyUser ? dataScopeService.currentUserId() : null;

        OrderSpecification spec = new OrderSpecification(
                storeId, statuses, fromDate, toDate, driverId, paymentMethod, paymentStatus,
                deliveredFromDate, deliveredToDate, createdByUserId, overdueCredit, storeSearch,
                visibleCompanyDraftCreatorId);
        Sort sort = Sort.by(Sort.Direction.DESC, "orderDate");

        if (page == null || pageSize == null) {
            List<Order> orders = orderRepository.findAll(spec, sort);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("orders", orders.stream()
                            .map(order -> order.toListResponseDTO(companyUser)).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<Order> pageResult = orderRepository.findAll(spec, pageable);

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "orders", pageResult.getContent().stream()
                        .map(order -> order.toListResponseDTO(companyUser)).toList(),
                "pageSize", pageSize,
                "currentPage", page,
                "totalPages", pageResult.getTotalPages(),
                "totalElements", pageResult.getTotalElements()));
    }

    public ApiResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        assertCanAccess(order);
        return new ApiResponse("", HttpStatus.OK,
                Map.of("order", order.toSingleResponseDTO(dataScopeService.isCurrentUserCompanyUser())));
    }

    @Transactional
    public ApiResponse updateOrder(UUID id, UpdateOrderDTO dto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        assertCanAccess(order);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new CustomException("Only DRAFT orders can be edited", HttpStatus.BAD_REQUEST, "");
        }

        boolean companyUser = dataScopeService.isCurrentUserCompanyUser();
        if (!companyUser && (dto.getExpectedDeliveryDate() != null || dto.getPaymentDueDate() != null)) {
            throw new CustomException(
                    "Expected delivery date and payment due date are managed by the distribution company",
                    HttpStatus.FORBIDDEN,
                    "company_only_order_schedule");
        }

        if (dto.getPaymentMethod() != null) {
            order.setPaymentMethod(dto.getPaymentMethod());
            if (dto.getPaymentMethod() != PaymentMethod.CREDIT) {
                order.setPaymentDueDate(null);
            }
        }
        if (companyUser && dto.getExpectedDeliveryDate() != null) {
            validateExpectedDeliveryDate(dto.getExpectedDeliveryDate());
            order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        }
        if (companyUser && dto.getPaymentDueDate() != null) {
            validatePaymentDueDate(order.getPaymentMethod(), dto.getPaymentDueDate(), false);
            order.setPaymentDueDate(dto.getPaymentDueDate());
        }
        if (dto.getDeliveryAddress() != null) order.setDeliveryAddress(dto.getDeliveryAddress());
        if (dto.getNotes() != null) order.setNotes(dto.getNotes());

        if (dto.getItems() != null) {
            if (dto.getTotalPrice() == null) {
                throw new CustomException("Total price is required when editing order items",
                        HttpStatus.BAD_REQUEST, "");
            }
            List<OrderItem> replacementItems = buildItems(order, dto.getItems());
            BigDecimal replacementTotal = calculateTotal(replacementItems);
            verifySubmittedTotal(dto.getTotalPrice(), replacementTotal);
            order.getItems().clear();
            order.getItems().addAll(replacementItems);
            order.setTotalAmount(replacementTotal);
        } else if (dto.getTotalPrice() != null
                && dto.getTotalPrice().compareTo(order.getTotalAmount()) != 0) {
            throw new CustomException("Total price can only change when order items are supplied",
                    HttpStatus.BAD_REQUEST, "");
        }

        validatePaymentDueDate(order.getPaymentMethod(), order.getPaymentDueDate(), false);

        orderRepository.save(order);
        return new ApiResponse("Order updated successfully", HttpStatus.OK,
                Map.of("order", order.toSingleResponseDTO(companyUser)));
    }

    @Transactional
    public ApiResponse confirmOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        assertCanAccess(order);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new CustomException("Only DRAFT orders can be submitted", HttpStatus.BAD_REQUEST, "");
        }

        changeStatus(order, OrderStatus.SUBMITTED, null);
        orderRepository.save(order);
        return new ApiResponse("Order submitted successfully", HttpStatus.OK,
                Map.of("order", order.toSingleResponseDTO(dataScopeService.isCurrentUserCompanyUser())));
    }

    @Transactional
    public ApiResponse approveOrder(UUID id) {
        dataScopeService.requireCompanyUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        assertCanAccess(order);

        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new CustomException("Only SUBMITTED orders can be approved", HttpStatus.BAD_REQUEST, "");
        }

        changeStatus(order, OrderStatus.APPROVED, null);
        orderRepository.save(order);
        return new ApiResponse("Order approved successfully", HttpStatus.OK,
                Map.of("order", order.toSingleResponseDTO()));
    }

    @Transactional
    public ApiResponse assignDriver(UUID id, UUID driverId) {
        dataScopeService.requireCompanyUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        assertCanAccess(order);

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new CustomException("A driver can only be assigned to APPROVED orders", HttpStatus.BAD_REQUEST, "");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DataNotFoundException("Driver not found"));

        if (!driver.isActive() || driver.isDeactivated()) {
            throw new CustomException("Cannot assign an inactive driver", HttpStatus.BAD_REQUEST, "");
        }

        order.setDriver(driver);
        changeStatus(order, OrderStatus.PROCESSING, null);
        orderRepository.save(order);
        return new ApiResponse("Driver assigned successfully", HttpStatus.OK,
                Map.of("order", order.toSingleResponseDTO()));
    }

    @Transactional
    public ApiResponse dispatchOrder(UUID id, DispatchOrderDTO dto) {
        dataScopeService.requireCompanyUser();
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        assertCanAccess(order);

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new CustomException("Only PROCESSING orders can be dispatched", HttpStatus.BAD_REQUEST, "");
        }

        LocalDateTime expectedDeliveryDate = dto.getExpectedDeliveryDate() != null
                ? dto.getExpectedDeliveryDate() : order.getExpectedDeliveryDate();
        if (expectedDeliveryDate == null) {
            throw new CustomException("Expected delivery date is required when dispatching an order",
                    HttpStatus.BAD_REQUEST, "");
        }
        LocalDate paymentDueDate = dto.getPaymentDueDate() != null
                ? dto.getPaymentDueDate() : order.getPaymentDueDate();
        validateExpectedDeliveryDate(expectedDeliveryDate);
        validatePaymentDueDate(order.getPaymentMethod(), paymentDueDate, true);
        order.setExpectedDeliveryDate(expectedDeliveryDate);
        order.setPaymentDueDate(order.getPaymentMethod() == PaymentMethod.CREDIT
                ? paymentDueDate : null);

        Map<UUID, Integer> quantitiesByProduct = aggregateItemQuantities(order);
        List<Product> products = lockAndValidateProducts(quantitiesByProduct);

        for (Product product : products) {
            int quantityDispatched = quantitiesByProduct.get(product.getId());
            product.setQuantity(product.getQuantity() - quantityDispatched);
            productHistoryRepository.save(ProductHistory.builder()
                    .product(product)
                    .historyMode(HistoryMode.SALE)
                    .quantityChanged(-quantityDispatched)
                    .note("Dispatched order " + order.getId())
                    .build());
        }
        productRepository.saveAll(products);

        changeStatus(order, OrderStatus.DISPATCHED, null);
        orderRepository.save(order);
        return new ApiResponse("Order dispatched successfully", HttpStatus.OK,
                Map.of("order", order.toSingleResponseDTO()));
    }

    @Transactional
    public ApiResponse deliverOrder(UUID id) {
        dataScopeService.requireCompanyUser();
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        assertCanAccess(order);

        if (order.getStatus() != OrderStatus.DISPATCHED) {
            throw new CustomException("Only DISPATCHED orders can be marked as delivered", HttpStatus.BAD_REQUEST, "");
        }

        changeStatus(order, OrderStatus.DELIVERED, null);
        order.setDeliveredAt(LocalDateTime.now());

        // Cash orders are automatically paid on delivery
        if (order.getPaymentMethod() == PaymentMethod.CASH) {
            order.setPaymentStatus(PaymentStatus.PAID);
            StoreTransaction transaction = StoreTransaction.builder()
                    .store(order.getStore())
                    .transactionType(TransactionType.CASH_PAYMENT)
                    .amount(order.getTotalAmount())
                    .order(order)
                    .note("Cash payment on delivery")
                    .build();
            storeTransactionRepository.save(transaction);
        } else {
            order.setPaymentStatus(PaymentStatus.UNPAID);
            // Credit order — record the credit against the store
            StoreTransaction transaction = StoreTransaction.builder()
                    .store(order.getStore())
                    .transactionType(TransactionType.CREDIT)
                    .amount(order.getTotalAmount())
                    .order(order)
                    .note("Credit on delivery")
                    .build();
            storeTransactionRepository.save(transaction);
        }

        orderRepository.save(order);
        return new ApiResponse("Order delivered successfully", HttpStatus.OK,
                Map.of("order", order.toSingleResponseDTO()));
    }

    @Transactional
    public ApiResponse returnOrder(UUID id, String reason) {
        dataScopeService.requireCompanyUser();
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        assertCanAccess(order);

        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.DISPATCHED) {
            throw new CustomException("Only Dispatched and Delivered orders can be returned", HttpStatus.BAD_REQUEST, "");
        }
        boolean wasDelivered = order.getStatus() == OrderStatus.DELIVERED;

        Map<UUID, Integer> quantitiesByProduct = aggregateItemQuantities(order);
        List<Product> products = productRepository.findAllByIdForUpdateIncludingInactive(
                new ArrayList<>(quantitiesByProduct.keySet()));

        if (products.size() != quantitiesByProduct.size()) {
            throw new DataNotFoundException("One or more products in the order are inactive or no longer exist");
        }

        for (Product product : products) {
            int quantityReturned = quantitiesByProduct.get(product.getId());
            product.setQuantity(product.getQuantity() + quantityReturned);
            productHistoryRepository.save(ProductHistory.builder()
                    .product(product)
                    .historyMode(HistoryMode.RETURN)
                    .quantityChanged(quantityReturned)
                    .note("Returned order " + order.getId())
                    .build());
        }
        productRepository.saveAll(products);

        if (wasDelivered) {
            StoreTransaction reversal = StoreTransaction.builder()
                    .store(order.getStore())
                    .transactionType(TransactionType.REFUND)
                    .amount(order.getTotalAmount())
                    .order(order)
                    .note("Return of order: " + reason)
                    .build();
            storeTransactionRepository.save(reversal);
        }

        changeStatus(order, OrderStatus.RETURNED, reason);
        order.setPaymentStatus(wasDelivered ? PaymentStatus.REFUNDED : PaymentStatus.UNPAID);
        orderRepository.save(order);

        return new ApiResponse("Order returned successfully", HttpStatus.OK,
                Map.of("order", order.toSingleResponseDTO()));
    }

    private Map<UUID, Integer> aggregateItemQuantities(Order order) {
        Map<UUID, Integer> quantitiesByProduct = new LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            quantitiesByProduct.merge(item.getProduct().getId(), item.getQuantity(), Integer::sum);
        }
        return quantitiesByProduct;
    }

    private List<Product> lockAndValidateProducts(Map<UUID, Integer> quantitiesByProduct) {
        List<Product> products = productRepository.findAllActiveByIdForUpdate(
                new ArrayList<>(quantitiesByProduct.keySet()));

        if (products.size() != quantitiesByProduct.size()) {
            throw new DataNotFoundException("One or more products in the order no longer exist");
        }

        for (Product product : products) {
            int requested = quantitiesByProduct.get(product.getId());
            if (product.getQuantity() < requested) {
                throw new CustomException(
                        "Insufficient stock for " + product.getName()
                                + ": requested " + requested + ", available " + product.getQuantity(),
                        HttpStatus.BAD_REQUEST,
                        "");
            }
        }
        return products;
    }

    public ApiResponse getDriverOrders(UUID driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new DataNotFoundException("Driver not found");
        }
        List<Order> orders = orderRepository.findByDriverIdOrderByOrderDateDesc(driverId);
        UUID requesterStoreId = dataScopeService.currentStoreId();
        if (requesterStoreId != null) {
            orders = orders.stream()
                    .filter(order -> order.getStore() != null
                            && requesterStoreId.equals(order.getStore().getId()))
                    .toList();
        }

        return new ApiResponse("", HttpStatus.OK,
                Map.of("orders", orders.stream().map(Order::toListResponseDTO).toList()));
    }

    @Transactional
    public ApiResponse cancelOrder(UUID id, String reason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        assertCanAccess(order);

        if (order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.DISPATCHED
                || order.getStatus() == OrderStatus.RETURNED) {
            throw new CustomException("Cannot cancel a " + order.getStatus().name().toLowerCase() + " order",
                    HttpStatus.BAD_REQUEST, "");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new CustomException("Order is already cancelled", HttpStatus.BAD_REQUEST, "");
        }

        changeStatus(order, OrderStatus.CANCELLED, reason);
        orderRepository.save(order);
        return new ApiResponse("Order cancelled successfully", HttpStatus.OK,
                Map.of("order", order.toSingleResponseDTO(dataScopeService.isCurrentUserCompanyUser())));
    }

    @Transactional
    public ApiResponse deleteOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        assertCanAccess(order);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new CustomException("Only DRAFT orders can be deleted", HttpStatus.BAD_REQUEST, "");
        }

        orderRepository.delete(order);
        return new ApiResponse("Order deleted successfully", HttpStatus.OK);
    }

    public ApiResponse getStatusHistory(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        assertCanAccess(order);
        return new ApiResponse("", HttpStatus.OK, Map.of(
                "history", orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(id)
                        .stream().map(OrderStatusHistory::toResponseDTO).toList()));
    }

    private void assertCanAccess(Order order) {
        UUID requesterStoreId = dataScopeService.currentStoreId();
        if (requesterStoreId != null) {
            if (order.getStore() == null) {
                throw draftAccessDenied();
            }
            dataScopeService.assertCanAccessStore(order.getStore().getId());
            return;
        }

        if (order.getStatus() == OrderStatus.DRAFT) {
            UUID creatorId = order.getCreatedBy() != null ? order.getCreatedBy().getId() : null;
            if (creatorId == null || !creatorId.equals(dataScopeService.currentUserId())) {
                throw draftAccessDenied();
            }
        }
    }

    private CustomException draftAccessDenied() {
        return new CustomException(
                "Draft orders are only visible to the target store and the company user who created the draft",
                HttpStatus.FORBIDDEN,
                "draft_order_access_denied");
    }

    private void validateCreateScheduling(CreateOrderDTO dto, boolean companyUser) {
        if (!companyUser && (dto.getExpectedDeliveryDate() != null || dto.getPaymentDueDate() != null)) {
            throw new CustomException(
                    "Expected delivery date and payment due date are managed by the distribution company",
                    HttpStatus.FORBIDDEN,
                    "company_only_order_schedule");
        }
        if (companyUser && dto.getExpectedDeliveryDate() != null) {
            validateExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        }
        if (companyUser) {
            validatePaymentDueDate(dto.getPaymentMethod(), dto.getPaymentDueDate(), false);
        }
    }

    private void validateExpectedDeliveryDate(LocalDateTime expectedDeliveryDate) {
        if (expectedDeliveryDate.isBefore(LocalDateTime.now())) {
            throw new CustomException("Expected delivery date cannot be in the past",
                    HttpStatus.BAD_REQUEST, "");
        }
    }

    private void validatePaymentDueDate(PaymentMethod paymentMethod, LocalDate paymentDueDate,
                                        boolean requiredForCredit) {
        if (paymentMethod == PaymentMethod.CREDIT) {
            if (requiredForCredit && paymentDueDate == null) {
                throw new CustomException("Payment due date is required for credit orders", HttpStatus.BAD_REQUEST, "");
            }
            if (paymentDueDate != null && paymentDueDate.isBefore(LocalDate.now())) {
                throw new CustomException("Payment due date cannot be in the past", HttpStatus.BAD_REQUEST, "");
            }
        } else if (paymentDueDate != null) {
            throw new CustomException("Payment due date is only allowed for credit orders", HttpStatus.BAD_REQUEST, "");
        }
    }

    private List<OrderItem> buildItems(Order order, List<OrderItemRequestDTO> itemDTOs) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequestDTO itemDTO : itemDTOs) {
            Product product = productRepository.findByIdAndActiveTrue(itemDTO.getProductId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "Active product not found: " + itemDTO.getProductId()));
            BigDecimal unitPrice = itemDTO.getUnitPrice() != null
                    ? itemDTO.getUnitPrice()
                    : product.getSellingPrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            items.add(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build());
        }
        return items;
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void verifySubmittedTotal(BigDecimal submittedTotal, BigDecimal calculatedTotal) {
        if (submittedTotal.compareTo(calculatedTotal) != 0) {
            throw new CustomException(
                    "Order total mismatch: submitted " + submittedTotal + ", calculated " + calculatedTotal,
                    HttpStatus.BAD_REQUEST,
                    "Recalculate the total from each item's unit price and quantity");
        }
    }

    private void changeStatus(Order order, OrderStatus newStatus, String reason) {
        OrderStatus previousStatus = order.getStatus();
        order.setStatus(newStatus);
        recordStatusChange(order, previousStatus, newStatus, reason);
    }

    private void recordStatusChange(Order order, OrderStatus previousStatus,
                                    OrderStatus newStatus, String reason) {
        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build());
    }
}
