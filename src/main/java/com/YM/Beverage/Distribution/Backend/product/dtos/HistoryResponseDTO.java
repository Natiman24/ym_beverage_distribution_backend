package com.YM.Beverage.Distribution.Backend.product.dtos;

import com.YM.Beverage.Distribution.Backend.product.enums.HistoryMode;
import com.YM.Beverage.Distribution.Backend.supplier.dtos.ListSupplierResponseDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.UserResponseDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryResponseDTO {
    private UUID id;
    private HistoryMode historyMode;
    private int quantityChanged;
    private ListSupplierResponseDTO supplier;
    private String note;
    private UserResponseDTO createdBy;
    private UserResponseDTO updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
