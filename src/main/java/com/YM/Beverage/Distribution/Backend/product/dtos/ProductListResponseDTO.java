package com.YM.Beverage.Distribution.Backend.product.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResponseDTO {
    private UUID id;
    private String name;
    private String brand;
    private String category;
    private String unit;
    private BigDecimal volume;
    private String volumeUnit;
    private Integer quantity;
    private Integer thresholdQuantity;
    private BigDecimal sellingPrice;
    private Boolean active;
    private Long version;
}
