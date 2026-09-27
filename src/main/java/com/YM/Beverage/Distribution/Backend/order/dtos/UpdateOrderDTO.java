package com.YM.Beverage.Distribution.Backend.order.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderDTO {

    private LocalDateTime expectedDeliveryDate;

    private String deliveryAddress;

    private String notes;
}
