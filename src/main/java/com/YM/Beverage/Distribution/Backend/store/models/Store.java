package com.YM.Beverage.Distribution.Backend.store.models;

import com.YM.Beverage.Distribution.Backend.store.dtos.ListStoreResponseDTO;
import com.YM.Beverage.Distribution.Backend.store.dtos.SingleStoreResponseDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.AuditUserResponseDTO;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity(name = "stores")
@Inheritance(strategy = InheritanceType.JOINED)
public class Store extends BaseEntity {
    private String name;

    private String description;

    @Column(name = "tin_number", unique = true)
    private String tinNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String email;

    private String city;

    @Column(name = "sub_city")
    private String subCity;

    private String address;

    private boolean active = true;

    public SingleStoreResponseDTO toSingleResponseDTO() {
        return SingleStoreResponseDTO.builder()
                .id(getId())
                .name(getName())
                .description(getDescription())
                .phoneNumber(getPhoneNumber())
                .tinNumber(getTinNumber())
                .email(getEmail())
                .city(getCity())
                .subCity(getSubCity())
                .address(getAddress())
                .active(isActive())
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .createdBy(toAuditUserResponseDTO(getCreatedBy()))
                .updatedBy(toAuditUserResponseDTO(getUpdatedBy()))
                .build();
    }

    private AuditUserResponseDTO toAuditUserResponseDTO(com.YM.Beverage.Distribution.Backend.user.models.User user) {
        if (user == null) {
            return null;
        }
        return AuditUserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    public ListStoreResponseDTO toListResponseDTO() {
        return ListStoreResponseDTO.builder()
                .id(getId())
                .name(getName())
                .description(getDescription())
                .phoneNumber(getPhoneNumber())
                .tinNumber(getTinNumber())
                .email(getEmail())
                .city(getCity())
                .subCity(getSubCity())
                .address(getAddress())
                .active(isActive())
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
