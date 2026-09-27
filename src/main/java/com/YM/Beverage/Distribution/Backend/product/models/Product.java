package com.YM.Beverage.Distribution.Backend.product.models;

import com.YM.Beverage.Distribution.Backend.product.dtos.ProductListResponseDTO;
import com.YM.Beverage.Distribution.Backend.product.dtos.ProductSingleResponseDTO;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private ProductUnit unit;

    /**
     * Example:
     * 330 ml
     * 500 ml
     * 1 L
     * 2 L
     */
    private BigDecimal volume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volume_unit_id")
    private VolumeUnit volumeUnit;

    /**
     * Number of individual units inside one pack/carton/crate.
     *
     * Example:
     * Coca-Cola 500ml carton = 24 bottles
     */
    private Integer unitsPerPack;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "threshold_quantity", nullable = false)
    @Builder.Default
    private Integer thresholdQuantity = 10;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal sellingPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Version
    @Column(nullable = false)
    private Long version;

    public ProductSingleResponseDTO toSingleResponseDTO() {
        return ProductSingleResponseDTO.builder()
                .id(getId())
                .name(name)
                .description(description)
                .brand(brand != null ? brand.toResponseDTO() : null)
                .category(category != null ? category.toResponseDTO() : null)
                .unit(unit != null ? unit.toResponseDTO() : null)
                .volume(volume)
                .volumeUnit(volumeUnit != null ? volumeUnit.toResponseDTO() : null)
                .unitsPerPack(unitsPerPack)
                .quantity(quantity)
                .thresholdQuantity(thresholdQuantity)
                .sellingPrice(sellingPrice)
                .purchasePrice(purchasePrice)
                .active(active)
                .version(version)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }

    public ProductListResponseDTO toListResponseDTO() {
        return ProductListResponseDTO.builder()
                .id(getId())
                .name(name)
                .brand(brand != null ? brand.getName() : null)
                .category(category != null ? category.getName() : null)
                .unit(unit != null ? unit.getName() : null)
                .volume(volume)
                .volumeUnit(volumeUnit != null ? volumeUnit.getName() : null)
                .quantity(quantity)
                .thresholdQuantity(thresholdQuantity)
                .sellingPrice(sellingPrice)
                .active(active)
                .version(version)
                .build();
    }
}
