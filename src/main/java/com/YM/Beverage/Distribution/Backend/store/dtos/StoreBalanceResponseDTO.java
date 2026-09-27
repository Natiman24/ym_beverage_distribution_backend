package com.YM.Beverage.Distribution.Backend.store.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreBalanceResponseDTO {
    private UUID storeId;
    private String storeName;
    private BigDecimal totalCredit;
    private BigDecimal totalRepaid;
    private BigDecimal outstandingBalance;
}
