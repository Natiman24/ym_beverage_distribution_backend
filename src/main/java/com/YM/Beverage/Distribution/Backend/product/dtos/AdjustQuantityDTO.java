package com.YM.Beverage.Distribution.Backend.product.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustQuantityDTO {

    /**
     * Positive value increases stock, negative value decreases stock.
     */
    @NotNull(message = "Adjustment amount is required")
    private Integer amount;

    private UUID supplierId;

    private String note;
}
