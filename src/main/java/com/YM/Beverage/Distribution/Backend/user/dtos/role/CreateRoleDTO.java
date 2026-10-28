package com.YM.Beverage.Distribution.Backend.user.dtos.role;

import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import com.YM.Beverage.Distribution.Backend.user.models.PermissionScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoleDTO {
    @NotNull
    @NotBlank
    private String name;

    private Set<Permission> permissions;
    private Map<Permission, PermissionScope> permissionScopes;
}
