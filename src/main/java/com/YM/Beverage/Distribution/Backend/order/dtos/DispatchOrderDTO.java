package com.YM.Beverage.Distribution.Backend.order.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchOrderDTO {

    private LocalDateTime expectedDeliveryDate;

    private LocalDate paymentDueDate;
}
