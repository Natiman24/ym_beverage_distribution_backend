package com.YM.Beverage.Distribution.Backend.order.models;

import com.YM.Beverage.Distribution.Backend.order.dtos.OrderStatusHistoryResponseDTO;
import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "order_status_histories")
public class OrderStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private OrderStatus newStatus;

    @Column(columnDefinition = "TEXT")
    private String reason;

    public OrderStatusHistoryResponseDTO toResponseDTO() {
        return OrderStatusHistoryResponseDTO.builder()
                .id(getId())
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .changedBy(getCreatedBy() != null ? getCreatedBy().toUserResponseDTO() : null)
                .changedAt(getCreatedAt())
                .build();
    }
}
