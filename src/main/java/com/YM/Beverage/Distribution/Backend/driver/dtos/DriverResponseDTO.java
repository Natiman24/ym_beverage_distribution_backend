package com.YM.Beverage.Distribution.Backend.driver.dtos;

import com.YM.Beverage.Distribution.Backend.user.dtos.profile.AuditUserResponseDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Set;
import com.YM.Beverage.Distribution.Backend.user.dtos.role.RoleResponseDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String licenseNumber;
    private boolean availableForDelivery;
    private boolean active;
    private boolean isDeactivated;
    private Set<RoleResponseDTO> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AuditUserResponseDTO createdBy;
    private AuditUserResponseDTO updatedBy;
}
