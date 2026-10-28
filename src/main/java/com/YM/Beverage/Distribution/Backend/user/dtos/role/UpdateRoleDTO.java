package com.YM.Beverage.Distribution.Backend.user.dtos.role;

import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import com.YM.Beverage.Distribution.Backend.user.models.PermissionScope;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Set;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateRoleDTO {
    private String name;
    private Set<Permission> permissions;
    private Map<Permission, PermissionScope> permissionScopes;
}
