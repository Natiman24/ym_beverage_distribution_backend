package com.YM.Beverage.Distribution.Backend.order.dtos;

import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.UserResponseDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistoryResponseDTO {
    private UUID id;
    private OrderStatus previousStatus;
    private OrderStatus newStatus;
    private String reason;
    private UserResponseDTO changedBy;
    private LocalDateTime changedAt;
}
