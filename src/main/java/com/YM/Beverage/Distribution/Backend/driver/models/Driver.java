package com.YM.Beverage.Distribution.Backend.driver.models;

import com.YM.Beverage.Distribution.Backend.driver.dtos.DriverResponseDTO;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "drivers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Driver extends User {

    @Column(unique = true)
    private String licenseNumber;

    public DriverResponseDTO toResponseDTO() {
        return DriverResponseDTO.builder()
                .id(getId())
                .firstName(getFirstName())
                .lastName(getLastName())
                .phoneNumber(getPhoneNumber())
                .email(getEmail())
                .licenseNumber(licenseNumber)
                .active(isActive())
                .isDeactivated(isDeactivated())
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
