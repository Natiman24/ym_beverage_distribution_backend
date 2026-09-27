
package com.YM.Beverage.Distribution.Backend.user.dtos.profile;

import com.YM.Beverage.Distribution.Backend.user.dtos.role.RoleResponseDTO;
import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import lombok.*;

import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private boolean isActive;
    private boolean isDeactivated;
    private Set<RoleResponseDTO> roles;
    private Set<Permission> permissions;
    private UUID storeId;
    private String storeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AuditUserResponseDTO createdBy;
    private AuditUserResponseDTO updatedBy;
}
