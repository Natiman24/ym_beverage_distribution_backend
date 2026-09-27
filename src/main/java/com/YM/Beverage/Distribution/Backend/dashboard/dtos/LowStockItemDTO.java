package com.YM.Beverage.Distribution.Backend.dashboard.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockItemDTO {
    private UUID productId;
    private String productName;
    private int quantity;
}
