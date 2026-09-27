package com.YM.Beverage.Distribution.Backend.order.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderDTO {
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}
