package com.YM.Beverage.Distribution.Backend.store.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentAllocationDTO {

    @NotNull(message = "Order ID is required in allocation")
    private UUID orderId;

    @NotNull(message = "Allocation amount is required")
    @Positive(message = "Allocation amount must be positive")
    private BigDecimal amount;
}
