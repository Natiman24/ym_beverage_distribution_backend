package com.YM.Beverage.Distribution.Backend.product.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProductDTO {
    private String name;
    private String description;
    private UUID brandId;
    private UUID categoryId;
    private UUID unitId;

    @Positive(message = "Volume must be positive")
    private BigDecimal volume;

    private UUID volumeUnitId;

    @Positive(message = "Units per pack must be positive")
    private Integer unitsPerPack;

    @PositiveOrZero(message = "Threshold quantity must be zero or positive")
    private Integer thresholdQuantity;

    @Positive(message = "Selling price must be positive")
    private BigDecimal sellingPrice;

    @Positive(message = "Purchase price must be positive")
    private BigDecimal purchasePrice;

}
