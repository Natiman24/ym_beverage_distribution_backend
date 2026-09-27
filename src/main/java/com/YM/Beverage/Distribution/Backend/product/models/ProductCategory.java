package com.YM.Beverage.Distribution.Backend.product.models;

import com.YM.Beverage.Distribution.Backend.product.dtos.LookupResponseDTO;
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
@Table(name = "product_categories")
public class ProductCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    public LookupResponseDTO toResponseDTO() {
        return LookupResponseDTO.builder()
                .id(getId())
                .name(name)
                .description(description)
                .active(active)
                .build();
    }
}
