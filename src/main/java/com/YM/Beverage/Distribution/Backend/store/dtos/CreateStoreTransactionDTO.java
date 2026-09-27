package com.YM.Beverage.Distribution.Backend.store.dtos;

import com.YM.Beverage.Distribution.Backend.store.enums.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStoreTransactionDTO {

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    /**
     * Required for CREDIT and CASH_PAYMENT.
     * Optional for CREDIT_REPAYMENT and REFUND when paying a specific order.
     * Omit for bulk repayment — use allocations instead.
     */
    private UUID orderId;

    /**
     * Used only for bulk CREDIT_REPAYMENT (when orderId is null).
     * Must sum exactly to amount.
     */
    @Valid
    private List<RepaymentAllocationDTO> allocations;

    private String note;
}
