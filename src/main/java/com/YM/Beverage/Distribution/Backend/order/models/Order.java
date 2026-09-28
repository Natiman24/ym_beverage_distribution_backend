package com.YM.Beverage.Distribution.Backend.order.models;

import com.YM.Beverage.Distribution.Backend.driver.models.Driver;
import com.YM.Beverage.Distribution.Backend.order.dtos.OrderListResponseDTO;
import com.YM.Beverage.Distribution.Backend.order.dtos.OrderSingleResponseDTO;
import com.YM.Beverage.Distribution.Backend.order.enums.OrderStatus;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentMethod;
import com.YM.Beverage.Distribution.Backend.order.enums.PaymentStatus;
import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    private LocalDateTime expectedDeliveryDate;

    private LocalDateTime deliveredAt;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    private String deliveryAddress;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Version
    @Column(nullable = false)
    private Long version;

    public OrderSingleResponseDTO toSingleResponseDTO() {
        return toSingleResponseDTO(true);
    }

    public OrderSingleResponseDTO toSingleResponseDTO(boolean includeCompanyDetails) {
        return com.YM.Beverage.Distribution.Backend.order.dtos.OrderSingleResponseDTO.builder()
                .id(getId())
                .storeId(store != null ? store.getId() : null)
                .storeName(store != null ? store.getName() : null)
                .status(status)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .paymentDueDate(includeCompanyDetails ? paymentDueDate : null)
                .orderDate(orderDate)
                .expectedDeliveryDate(includeCompanyDetails ? expectedDeliveryDate : null)
                .deliveredAt(deliveredAt)
                .totalAmount(totalAmount)
                .deliveryAddress(deliveryAddress)
                .notes(notes)
                .driver(driver != null ? driver.toResponseDTO() : null)
                .items(items.stream().map(OrderItem::toResponseDTO).toList())
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .version(version)
                .build();
    }

    public OrderListResponseDTO toListResponseDTO() {
        return toListResponseDTO(true);
    }

    public OrderListResponseDTO toListResponseDTO(boolean includeCompanyDetails) {
        return com.YM.Beverage.Distribution.Backend.order.dtos.OrderListResponseDTO.builder()
                .id(getId())
                .storeId(store != null ? store.getId() : null)
                .storeName(store != null ? store.getName() : null)
                .status(status)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .paymentDueDate(includeCompanyDetails ? paymentDueDate : null)
                .orderDate(orderDate)
                .expectedDeliveryDate(includeCompanyDetails ? expectedDeliveryDate : null)
                .deliveredAt(deliveredAt)
                .totalAmount(totalAmount)
                .driverName(driver != null ? driver.getFirstName() + " " + driver.getLastName() : null)
                .itemCount(items != null ? items.size() : 0)
                .version(version)
                .build();
    }
}
