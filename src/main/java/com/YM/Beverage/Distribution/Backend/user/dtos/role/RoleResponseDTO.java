package com.YM.Beverage.Distribution.Backend.user.dtos.role;

import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponseDTO {
    private UUID id;
    private String name;
    private Set<Permission> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
