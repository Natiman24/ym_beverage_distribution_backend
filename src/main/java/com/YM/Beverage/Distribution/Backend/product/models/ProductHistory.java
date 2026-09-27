package com.YM.Beverage.Distribution.Backend.product.models;

import com.YM.Beverage.Distribution.Backend.product.dtos.HistoryResponseDTO;
import com.YM.Beverage.Distribution.Backend.product.enums.HistoryMode;
import com.YM.Beverage.Distribution.Backend.supplier.models.Supplier;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "product_histories")
public class ProductHistory extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "history_mode")
    private HistoryMode historyMode;

    @Column(name = "quantity_changed")
    private int quantityChanged;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = true)
    private Supplier supplier;

    @Column(columnDefinition = "TEXT")
    private String note = "";

    public HistoryResponseDTO toResponseDTO() {
        return HistoryResponseDTO.builder()
                .id(getId())
                .historyMode(historyMode)
                .quantityChanged(quantityChanged)
                .supplier(supplier != null ? supplier.toListResponseDTO() : null)
                .note(note)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .createdBy(getCreatedBy() != null ? getCreatedBy().toUserResponseDTO() : null)
                .updatedBy(getUpdatedBy() != null ? getUpdatedBy().toUserResponseDTO() : null)
                .build();
    }

}
