package com.YM.Beverage.Distribution.Backend.user.dtos.profile;

import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRolesPermissionsDTO {
    private Set<UUID> roleIds;
    private Set<Permission> permissions;
}
