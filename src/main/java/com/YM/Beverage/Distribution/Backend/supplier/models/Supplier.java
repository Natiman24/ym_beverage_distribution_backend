package com.YM.Beverage.Distribution.Backend.supplier.models;

import com.YM.Beverage.Distribution.Backend.supplier.dtos.ListSupplierResponseDTO;
import com.YM.Beverage.Distribution.Backend.supplier.dtos.SingleSupplierResponseDTO;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity(name = "suppliers")
@Inheritance(strategy = InheritanceType.JOINED)
public class Supplier extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;
    private String description;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    public SingleSupplierResponseDTO toSingleResponseDTO(){
        return SingleSupplierResponseDTO.builder().
                id(getId()).
                name(getName()).
                description(getDescription()).
                phoneNumber(getPhoneNumber()).
                active(getActive()).
                createdAt(getCreatedAt()).
                updatedAt(getUpdatedAt()).
                build();
    }

    public ListSupplierResponseDTO toListResponseDTO(){
        return ListSupplierResponseDTO.builder().
                id(getId()).
                name(getName()).
                description(getDescription()).
                phoneNumber(getPhoneNumber()).
                active(getActive()).
                build();
    }
}
