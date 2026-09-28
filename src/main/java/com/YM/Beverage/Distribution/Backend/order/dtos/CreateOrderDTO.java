package com.YM.Beverage.Distribution.Backend.order.dtos;

import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderDTO {

    /** When false (the default), the order is submitted immediately. */
    private boolean draft;

    @NotNull(message = "Store ID is required")
    private UUID storeId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private LocalDate paymentDueDate;

    @NotNull(message = "Total price is required")
    @jakarta.validation.constraints.Positive(message = "Total price must be positive")
    @jakarta.validation.constraints.Digits(integer = 17, fraction = 2,
            message = "Total price must have at most 2 decimal places")
    private BigDecimal totalPrice;

    private LocalDateTime expectedDeliveryDate;

    private String deliveryAddress;

    private String notes;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequestDTO> items;
}
