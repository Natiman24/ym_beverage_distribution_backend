package com.YM.Beverage.Distribution.Backend.store.repositories;

import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import com.YM.Beverage.Distribution.Backend.store.enums.TransactionType;
import com.YM.Beverage.Distribution.Backend.store.models.StoreTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StoreTransactionRepository extends JpaRepository<StoreTransaction, UUID> {

    List<StoreTransaction> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    Page<StoreTransaction> findByStoreIdOrderByCreatedAtDesc(UUID storeId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM StoreTransaction t WHERE t.store.id = :storeId AND t.transactionType = :type")
    BigDecimal sumAmountByStoreIdAndType(@Param("storeId") UUID storeId, @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM StoreTransaction t WHERE t.transactionType IN :types")
    BigDecimal sumAmountByTypes(@Param("types") List<TransactionType> types);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM StoreTransaction t WHERE t.order.id = :orderId AND t.transactionType IN :types")
    BigDecimal sumAmountByOrderIdAndTypes(@Param("orderId") UUID orderId, @Param("types") List<TransactionType> types);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM StoreTransaction t WHERE t.transactionType = :type")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM StoreTransaction t "
            + "WHERE t.transactionType = :type AND t.order.paymentMethod = :paymentMethod")
    BigDecimal sumAmountByTypeAndOrderPaymentMethod(
            @Param("type") TransactionType type,
            @Param("paymentMethod") PaymentMethod paymentMethod);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM StoreTransaction t "
            + "WHERE t.transactionType = :type AND t.order IS NULL")
    BigDecimal sumOrderlessAmountByType(@Param("type") TransactionType type);
}
