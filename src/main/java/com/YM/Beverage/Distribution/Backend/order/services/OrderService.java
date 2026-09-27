package com.YM.Beverage.Distribution.Backend.order.services;

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

    @Transactional
    public ApiResponse createOrder(CreateOrderDTO dto, UUID userId) {
        Store store = storeRepository.findByIdAndActiveTrue(dto.getStoreId())
                .orElseThrow(() -> new DataNotFoundException("Active store not found"));

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Requesting user not found"));

        boolean belongsToStore = requester.getStore() != null
                && requester.getStore().getId().equals(store.getId());
        boolean isSuperAdmin = requester.getRoles() != null
                && requester.getRoles().stream()
                .anyMatch(role -> "Super Admin".equalsIgnoreCase(role.getName()));

        if (!belongsToStore && !isSuperAdmin) {
            throw new CustomException("You are not authorized to create an order for this store", HttpStatus.FORBIDDEN, "");
        }

        validatePaymentDueDate(dto);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        Order order = Order.builder()
                .store(store)
                .status(OrderStatus.DRAFT)
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .paymentDueDate(dto.getPaymentMethod() == PaymentMethod.CREDIT ? dto.getPaymentDueDate() : null)
                .orderDate(LocalDateTime.now())
                .expectedDeliveryDate(dto.getExpectedDeliveryDate())
                .deliveryAddress(dto.getDeliveryAddress())
                .notes(dto.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (OrderItemRequestDTO itemDTO : dto.getItems()) {
            Product product = productRepository.findByIdAndActiveTrue(itemDTO.getProductId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "Active product not found: " + itemDTO.getProductId()));

            BigDecimal unitPrice = itemDTO.getUnitPrice() != null
                    ? itemDTO.getUnitPrice()
                    : product.getSellingPrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            subtotal = subtotal.add(totalPrice);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build();
            items.add(item);
        }

        order.setTotalAmount(subtotal);
        order.setItems(items);

        if (dto.getTotalPrice().compareTo(subtotal) != 0) {
            throw new CustomException(
                    "Order total mismatch: submitted " + dto.getTotalPrice() + ", calculated " + subtotal,
                    HttpStatus.BAD_REQUEST,
                    "Recalculate the total from each item's unit price and quantity");
        }

        orderRepository.save(order);
        recordStatusChange(order, null, OrderStatus.DRAFT, null);

        return new ApiResponse("Order created successfully", HttpStatus.CREATED,
                Map.of("order", order.toSingleResponseDTO()));
    }

    public ApiResponse getOrders(UUID storeId, List<OrderStatus> statuses,
                                  LocalDateTime fromDate, LocalDateTime toDate,
                                  UUID driverId, PaymentMethod paymentMethod,
                                  PaymentStatus paymentStatus,
                                  LocalDateTime deliveredFromDate, LocalDateTime deliveredToDate,
                                  UUID createdByUserId, Boolean overdueCredit, String storeSearch,
                                  Integer page, Integer pageSize) {

        OrderSpecification spec = new OrderSpecification(
                storeId, statuses, fromDate, toDate, driverId, paymentMethod, paymentStatus,
                deliveredFromDate, deliveredToDate, createdByUserId, overdueCredit, storeSearch);
        Sort sort = Sort.by(Sort.Direction.DESC, "orderDate");

        if (page == null || pageSize == null) {
            List<Order> orders = orderRepository.findAll(spec, sort);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("orders", orders.stream().map(Order::toListResponseDTO).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<Order> pageResult = orderRepository.findAll(spec, pageable);

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "orders", pageResult.getContent().stream().map(Order::toListResponseDTO).toList(),
                "pageSize", pageSize,
                "currentPage", page,
                "totalPages", pageResult.getTotalPages(),
                "totalElements", pageResult.getTotalElements()));
    }

    public ApiResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        return new ApiResponse("", HttpStatus.OK, Map.of("order", order.toSingleResponseDTO()));
    }

    @Transactional
    public ApiResponse updateOrder(UUID id, UpdateOrderDTO dto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.RETURNED
                || order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.DISPATCHED) {
            throw new CustomException("Cannot update a " + order.getStatus().name().toLowerCase() + " order", HttpStatus.BAD_REQUEST, "");
        }

        if (dto.getExpectedDeliveryDate() != null) order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        if (dto.getDeliveryAddress() != null) order.setDeliveryAddress(dto.getDeliveryAddress());
        if (dto.getNotes() != null) order.setNotes(dto.getNotes());

        orderRepository.save(order);
        return new ApiResponse("Order updated successfully", HttpStatus.OK,
                Map.of("order", order.toSingleResponseDTO()));
    }

    @Transactional
    public ApiResponse confirmOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new CustomException("Only DRAFT orders can be submitted", HttpStatus.BAD_REQUEST, "");
        }

        changeStatus(order, OrderStatus.SUBMITTED, null);
        orderRepository.save(order);
        return new ApiResponse("Order submitted successfully", HttpStatus.OK,
                Map.of("order", order.toSingleResponseDTO()));
    }

    @Transactional
    public ApiResponse approveOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

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
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

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
    public ApiResponse dispatchOrder(UUID id) {
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new CustomException("Only PROCESSING orders can be dispatched", HttpStatus.BAD_REQUEST, "");
        }

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
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

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
        Order order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

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
        return new ApiResponse("", HttpStatus.OK,
                Map.of("orders", orders.stream().map(Order::toListResponseDTO).toList()));
    }

    @Transactional
    public ApiResponse cancelOrder(UUID id, String reason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

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
                Map.of("order", order.toSingleResponseDTO()));
    }

    @Transactional
    public ApiResponse deleteOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new CustomException("Only DRAFT orders can be deleted", HttpStatus.BAD_REQUEST, "");
        }

        orderRepository.delete(order);
        return new ApiResponse("Order deleted successfully", HttpStatus.OK);
    }

    public ApiResponse getStatusHistory(UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new DataNotFoundException("Order not found");
        }
        return new ApiResponse("", HttpStatus.OK, Map.of(
                "history", orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(id)
                        .stream().map(OrderStatusHistory::toResponseDTO).toList()));
    }

    private void validatePaymentDueDate(CreateOrderDTO dto) {
        if (dto.getPaymentMethod() == PaymentMethod.CREDIT) {
            if (dto.getPaymentDueDate() == null) {
                throw new CustomException("Payment due date is required for credit orders", HttpStatus.BAD_REQUEST, "");
            }
            if (dto.getPaymentDueDate().isBefore(LocalDate.now())) {
                throw new CustomException("Payment due date cannot be in the past", HttpStatus.BAD_REQUEST, "");
            }
        } else if (dto.getPaymentDueDate() != null) {
            throw new CustomException("Payment due date is only allowed for credit orders", HttpStatus.BAD_REQUEST, "");
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
