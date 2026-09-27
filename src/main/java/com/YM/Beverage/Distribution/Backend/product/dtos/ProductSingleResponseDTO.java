package com.YM.Beverage.Distribution.Backend.product.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSingleResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private LookupResponseDTO brand;
    private LookupResponseDTO category;
    private LookupResponseDTO unit;
    private BigDecimal volume;
    private LookupResponseDTO volumeUnit;
    private Integer unitsPerPack;
    private Integer quantity;
    private Integer thresholdQuantity;
    private BigDecimal sellingPrice;
    private BigDecimal purchasePrice;
    private Boolean active;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
