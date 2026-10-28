package com.YM.Beverage.Distribution.Backend.driver.models;

import com.YM.Beverage.Distribution.Backend.driver.dtos.DriverResponseDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.AuditUserResponseDTO;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "drivers")
public class DriverProfile {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "available_for_delivery", nullable = false)
    @Builder.Default
    private boolean availableForDelivery = true;

    public DriverResponseDTO toResponseDTO() {
        return DriverResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .licenseNumber(licenseNumber)
                .availableForDelivery(availableForDelivery)
                .active(user.isActive() && !user.isDeactivated() && availableForDelivery)
                .isDeactivated(user.isDeactivated())
                .roles(user.getRoles() == null
                        ? java.util.Set.of()
                        : user.getRoles().stream().map(role -> role.toResponseDTO()).collect(java.util.stream.Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(toAuditUserResponseDTO(user.getCreatedBy()))
                .updatedBy(toAuditUserResponseDTO(user.getUpdatedBy()))
                .build();
    }

    private AuditUserResponseDTO toAuditUserResponseDTO(User auditUser) {
        if (auditUser == null) return null;
        return AuditUserResponseDTO.builder()
                .id(auditUser.getId())
                .firstName(auditUser.getFirstName())
                .lastName(auditUser.getLastName())
                .email(auditUser.getEmail())
                .build();
    }
}
