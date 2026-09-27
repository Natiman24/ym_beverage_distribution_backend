package com.YM.Beverage.Distribution.Backend.product.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateProductDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private UUID brandId;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @NotNull(message = "Unit is required")
    private UUID unitId;

    @Positive(message = "Volume must be positive")
    private BigDecimal volume;

    private UUID volumeUnitId;

    @Positive(message = "Units per pack must be positive")
    private Integer unitsPerPack;

    @PositiveOrZero(message = "Quantity must be zero or positive")
    private Integer quantity;

    @PositiveOrZero(message = "Threshold quantity must be zero or positive")
    private Integer thresholdQuantity;

    private UUID supplierId;

    @NotNull(message = "Selling price is required")
    @Positive(message = "Selling price must be positive")
    private BigDecimal sellingPrice;

    @Positive(message = "Purchase price must be positive")
    private BigDecimal purchasePrice;
}
