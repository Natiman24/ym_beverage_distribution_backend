package com.YM.Beverage.Distribution.Backend.store.services.Implementations;

import com.YM.Beverage.Distribution.Backend.order.models.Order;
import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentStatus;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderRepository;
import com.YM.Beverage.Distribution.Backend.store.dtos.*;
import com.YM.Beverage.Distribution.Backend.store.enums.TransactionType;
import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.store.models.StoreTransaction;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreRepository;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreTransactionRepository;
import com.YM.Beverage.Distribution.Backend.store.services.Interfaces.StoreTransactionService;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreTransactionServiceImpl implements StoreTransactionService {

    private static final List<TransactionType> PAYMENT_TYPES =
            List.of(TransactionType.CREDIT_REPAYMENT, TransactionType.REFUND, TransactionType.CASH_PAYMENT);

    private final StoreTransactionRepository transactionRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public ApiResponse createTransaction(UUID storeId, CreateStoreTransactionDTO dto) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Store not found"));

        // CREDIT and CASH_PAYMENT must always reference an order
        boolean orderRequired = dto.getTransactionType() == TransactionType.CREDIT
                || dto.getTransactionType() == TransactionType.CASH_PAYMENT;

        if (orderRequired && dto.getOrderId() == null) {
            throw new CustomException(
                    dto.getTransactionType().name() + " transactions must be linked to an order",
                    HttpStatus.BAD_REQUEST, "");
        }

        boolean isBulkRepayment = dto.getTransactionType() == TransactionType.CREDIT_REPAYMENT
                && dto.getOrderId() == null;

        if (isBulkRepayment) {
            return handleBulkRepayment(store, dto);
        }

        // Single-order path: CREDIT, CASH_PAYMENT, or pinned CREDIT_REPAYMENT/REFUND
        if (dto.getTransactionType() == TransactionType.CREDIT_REPAYMENT) {
            validateRepaymentAgainstOrder(storeId, dto.getOrderId(), dto.getAmount());
        }

        Order order = resolveAndValidateOrder(dto.getOrderId(), storeId);

        StoreTransaction transaction = StoreTransaction.builder()
                .store(store)
                .transactionType(dto.getTransactionType())
                .amount(dto.getAmount())
                .order(order)
                .note(dto.getNote())
                .build();

        transactionRepository.save(transaction);
        synchronizeOrderPaymentStatus(order);

        return new ApiResponse("Transaction recorded successfully", HttpStatus.CREATED,
                Map.of("transaction", transaction.toResponseDTO()));
    }

    @Override
    public ApiResponse getTransactions(UUID storeId, Integer page, Integer pageSize) {
        if (!storeRepository.existsById(storeId)) {
            throw new DataNotFoundException("Store not found");
        }

        if (page == null || pageSize == null) {
            List<StoreTransaction> transactions = transactionRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("transactions", transactions.stream().map(StoreTransaction::toResponseDTO).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<StoreTransaction> pageResult = transactionRepository.findByStoreIdOrderByCreatedAtDesc(storeId, pageable);

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "transactions", pageResult.getContent().stream().map(StoreTransaction::toResponseDTO).toList(),
                "pageSize", pageSize,
                "currentPage", page,
                "totalPages", pageResult.getTotalPages(),
                "totalElements", pageResult.getTotalElements()));
    }

    @Override
    public ApiResponse getStoreBalance(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Store not found"));

        BigDecimal totalCredit = transactionRepository.sumAmountByStoreIdAndType(storeId, TransactionType.CREDIT);
        BigDecimal totalRepaid = transactionRepository.sumAmountByStoreIdAndType(storeId, TransactionType.CREDIT_REPAYMENT)
                .add(transactionRepository.sumAmountByStoreIdAndType(storeId, TransactionType.REFUND));
        BigDecimal outstanding = totalCredit.subtract(totalRepaid);

        StoreBalanceResponseDTO balance = StoreBalanceResponseDTO.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .totalCredit(totalCredit)
                .totalRepaid(totalRepaid)
                .outstandingBalance(outstanding)
                .build();

        return new ApiResponse("", HttpStatus.OK, Map.of("balance", balance));
    }

    @Override
    public ApiResponse getOrderBalance(UUID storeId, UUID orderId) {
        if (!storeRepository.existsById(storeId)) {
            throw new DataNotFoundException("Store not found");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

        if (!order.getStore().getId().equals(storeId)) {
            throw new CustomException("Order does not belong to this store", HttpStatus.BAD_REQUEST, "");
        }

        BigDecimal orderTotal = order.getTotalAmount();
        BigDecimal totalPaid = transactionRepository.sumAmountByOrderIdAndTypes(orderId, PAYMENT_TYPES);
        BigDecimal remaining = orderTotal.subtract(totalPaid).max(BigDecimal.ZERO);

        OrderBalanceResponseDTO balance = OrderBalanceResponseDTO.builder()
                .orderId(orderId)
                .orderTotal(orderTotal)
                .totalPaid(totalPaid)
                .remainingBalance(remaining)
                .fullyPaid(remaining.compareTo(BigDecimal.ZERO) == 0)
                .build();

        return new ApiResponse("", HttpStatus.OK, Map.of("orderBalance", balance));
    }

    // --- Private helpers ---

    private ApiResponse handleBulkRepayment(Store store, CreateStoreTransactionDTO dto) {
        if (dto.getAllocations() == null || dto.getAllocations().isEmpty()) {
            throw new CustomException(
                    "Bulk CREDIT_REPAYMENT requires allocations specifying which orders to pay",
                    HttpStatus.BAD_REQUEST, "");
        }

        // Allocations must sum exactly to the total amount
        BigDecimal allocatedTotal = dto.getAllocations().stream()
                .map(RepaymentAllocationDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (allocatedTotal.compareTo(dto.getAmount()) != 0) {
            throw new CustomException(
                    "Allocation amounts (" + allocatedTotal + ") must sum exactly to the total amount (" + dto.getAmount() + ")",
                    HttpStatus.BAD_REQUEST, "");
        }

        // Validate store-level outstanding balance
        BigDecimal outstanding = computeOutstandingBalance(store.getId());
        if (dto.getAmount().compareTo(outstanding) > 0) {
            throw new CustomException(
                    "Repayment amount exceeds outstanding balance of " + outstanding,
                    HttpStatus.BAD_REQUEST, "");
        }

        List<StoreTransaction> savedTransactions = new ArrayList<>();

        for (RepaymentAllocationDTO allocation : dto.getAllocations()) {
            validateRepaymentAgainstOrder(store.getId(), allocation.getOrderId(), allocation.getAmount());
            Order order = resolveAndValidateOrder(allocation.getOrderId(), store.getId());

            StoreTransaction transaction = StoreTransaction.builder()
                    .store(store)
                    .transactionType(TransactionType.CREDIT_REPAYMENT)
                    .amount(allocation.getAmount())
                    .order(order)
                    .note(dto.getNote())
                    .build();

            transactionRepository.save(transaction);
            synchronizeOrderPaymentStatus(order);
            savedTransactions.add(transaction);
        }

        return new ApiResponse("Bulk repayment recorded successfully", HttpStatus.CREATED,
                Map.of("transactions", savedTransactions.stream().map(StoreTransaction::toResponseDTO).toList()));
    }

    private void validateRepaymentAgainstOrder(UUID storeId, UUID orderId, BigDecimal amount) {
        if (orderId == null) return;
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

        BigDecimal orderTotal = order.getTotalAmount();
        BigDecimal alreadyPaid = transactionRepository.sumAmountByOrderIdAndTypes(orderId, PAYMENT_TYPES);
        BigDecimal remaining = orderTotal.subtract(alreadyPaid);

        if (amount.compareTo(remaining) > 0) {
            throw new CustomException(
                    "Payment of " + amount + " exceeds remaining balance of " + remaining + " for order " + orderId,
                    HttpStatus.BAD_REQUEST, "");
        }
    }

    private Order resolveAndValidateOrder(UUID orderId, UUID storeId) {
        if (orderId == null) return null;
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found"));
        if (!order.getStore().getId().equals(storeId)) {
            throw new CustomException("Order does not belong to this store", HttpStatus.BAD_REQUEST, "");
        }
        return order;
    }

    private BigDecimal computeOutstandingBalance(UUID storeId) {
        BigDecimal totalCredit = transactionRepository.sumAmountByStoreIdAndType(storeId, TransactionType.CREDIT);
        BigDecimal totalRepaid = transactionRepository.sumAmountByStoreIdAndType(storeId, TransactionType.CREDIT_REPAYMENT)
                .add(transactionRepository.sumAmountByStoreIdAndType(storeId, TransactionType.REFUND));
        return totalCredit.subtract(totalRepaid);
    }

    private void synchronizeOrderPaymentStatus(Order order) {
        if (order == null) return;

        if (order.getStatus() == OrderStatus.RETURNED) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            BigDecimal totalPaid = transactionRepository.sumAmountByOrderIdAndTypes(order.getId(), PAYMENT_TYPES);
            if (totalPaid == null || totalPaid.compareTo(BigDecimal.ZERO) <= 0) {
                order.setPaymentStatus(PaymentStatus.UNPAID);
            } else if (totalPaid.compareTo(order.getTotalAmount()) < 0) {
                order.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
            } else {
                order.setPaymentStatus(PaymentStatus.PAID);
            }
        }
        orderRepository.save(order);
    }
}
