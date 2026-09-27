package com.YM.Beverage.Distribution.Backend.order.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;
import lombok.*;

import java.util.UUID;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequestDTO {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @Positive(message = "Unit price must be positive")
    @Digits(integer = 17, fraction = 2, message = "Unit price must have at most 2 decimal places")
    private BigDecimal unitPrice;
}
