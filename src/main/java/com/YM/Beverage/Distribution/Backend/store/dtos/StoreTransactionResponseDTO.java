package com.YM.Beverage.Distribution.Backend.store.dtos;

import com.YM.Beverage.Distribution.Backend.store.enums.TransactionType;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.UserResponseDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreTransactionResponseDTO {
    private UUID id;
    private UUID storeId;
    private String storeName;
    private TransactionType transactionType;
    private BigDecimal amount;
    private UUID orderId;
    private String note;
    private UserResponseDTO createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
