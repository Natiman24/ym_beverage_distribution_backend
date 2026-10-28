package com.YM.Beverage.Distribution.Backend.user.services;

import com.YM.Beverage.Distribution.Backend.user.dtos.role.CreateRoleDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.role.UpdateRoleDTO;
import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import com.YM.Beverage.Distribution.Backend.user.models.PermissionScope;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock private RoleRepository roleRepository;
    @Mock private UserRepository userRepository;

    @Test
    void newUnscopedPermissionsDefaultToAll() {
        CreateRoleDTO dto = CreateRoleDTO.builder()
                .name("Logistics")
                .permissions(Set.of(Permission.ORDER_VIEW))
                .build();
        when(roleRepository.findByNameIgnoreCase("Logistics")).thenReturn(Optional.empty());

        new RoleService(roleRepository, userRepository).createRole(dto);

        ArgumentCaptor<Role> role = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(role.capture());
        assertEquals(PermissionScope.ALL,
                role.getValue().getPermissionScopes().get(Permission.ORDER_VIEW));
    }

    @Test
    void legacyFrontendUpdatePreservesExistingScopes() {
        UUID id = UUID.randomUUID();
        Role role = Role.builder()
                .id(id)
                .name("Driver")
                .permissions(Set.of(Permission.ORDER_VIEW))
                .permissionScopes(Map.of(Permission.ORDER_VIEW, PermissionScope.ASSIGNED))
                .build();
        when(roleRepository.findById(id)).thenReturn(Optional.of(role));

        UpdateRoleDTO dto = UpdateRoleDTO.builder()
                .permissions(Set.of(Permission.ORDER_VIEW, Permission.STORE_VIEW))
                .build();
        new RoleService(roleRepository, userRepository).updateRole(id, dto);

        assertEquals(PermissionScope.ASSIGNED,
                role.getPermissionScopes().get(Permission.ORDER_VIEW));
        assertEquals(PermissionScope.ALL,
                role.getPermissionScopes().get(Permission.STORE_VIEW));
    }

    @Test
    void superAdminScopeCannotBeChanged() {
        UUID id = UUID.randomUUID();
        Role role = Role.builder().id(id).name("Super Admin").build();
        when(roleRepository.findById(id)).thenReturn(Optional.of(role));

        assertThrows(CustomException.class, () ->
                new RoleService(roleRepository, userRepository).updateRole(
                        id,
                        UpdateRoleDTO.builder()
                                .permissionScopes(Map.of(
                                        Permission.ORDER_VIEW,
                                        PermissionScope.ASSIGNED))
                                .build()));
    }
}
