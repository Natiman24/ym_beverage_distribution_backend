package com.YM.Beverage.Distribution.Backend.store.models;

import com.YM.Beverage.Distribution.Backend.order.models.Order;
import com.YM.Beverage.Distribution.Backend.store.dtos.StoreTransactionResponseDTO;
import com.YM.Beverage.Distribution.Backend.store.enums.TransactionType;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "store_transactions")
public class StoreTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Optional link to the order that triggered this transaction.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(columnDefinition = "TEXT")
    private String note;

    public StoreTransactionResponseDTO toResponseDTO() {
        return StoreTransactionResponseDTO.builder()
                .id(getId())
                .storeId(store != null ? store.getId() : null)
                .storeName(store != null ? store.getName() : null)
                .transactionType(transactionType)
                .amount(amount)
                .orderId(order != null ? order.getId() : null)
                .note(note)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .createdBy(getCreatedBy() != null ? getCreatedBy().toUserResponseDTO() : null)
                .build();
    }
}
