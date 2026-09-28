package com.YM.Beverage.Distribution.Backend.order.dtos;

import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderDTO {

    private LocalDateTime expectedDeliveryDate;

    private LocalDate paymentDueDate;

    private PaymentMethod paymentMethod;

    @Positive(message = "Total price must be positive")
    @Digits(integer = 17, fraction = 2,
            message = "Total price must have at most 2 decimal places")
    private BigDecimal totalPrice;

    private String deliveryAddress;

    private String notes;

    @Valid
    @Size(min = 1, message = "A draft order must have at least one item")
    private List<OrderItemRequestDTO> items;
}
