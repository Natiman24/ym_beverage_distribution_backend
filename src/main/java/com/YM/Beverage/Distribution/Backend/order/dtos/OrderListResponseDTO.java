package com.YM.Beverage.Distribution.Backend.order.dtos;

import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResponseDTO {
    private UUID id;
    private UUID storeId;
    private String storeName;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDate paymentDueDate;
    private LocalDateTime orderDate;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime deliveredAt;
    private BigDecimal totalAmount;
    private String driverName;
    private int itemCount;
    private Long version;
}
