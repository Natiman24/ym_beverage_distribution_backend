package com.YM.Beverage.Distribution.Backend.store.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBalanceResponseDTO {
    private UUID orderId;
    private BigDecimal orderTotal;
    private BigDecimal totalPaid;
    private BigDecimal remainingBalance;
    private boolean fullyPaid;
}
