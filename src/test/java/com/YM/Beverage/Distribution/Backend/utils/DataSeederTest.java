package com.YM.Beverage.Distribution.Backend.utils;

import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import com.YM.Beverage.Distribution.Backend.user.models.PermissionScope;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock private RoleRepository roleRepository;

    @Test
    void superAdminReceivesEveryPermissionAtAllScope() {
        Role superAdmin = Role.builder()
                .name("Super Admin")
                .permissions(new HashSet<>(Set.of(Permission.ORDER_VIEW)))
                .permissionScopes(new HashMap<>(Map.of(
                        Permission.ORDER_VIEW, PermissionScope.ASSIGNED)))
                .build();
        when(roleRepository.findByNameIgnoreCase("Super Admin"))
                .thenReturn(Optional.of(superAdmin));

        new DataSeeder(roleRepository).run(null);

        ArgumentCaptor<Role> saved = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(saved.capture());
        assertEquals(Permission.values().length, saved.getValue().getPermissions().size());
        assertTrue(saved.getValue().getPermissionScopes().values().stream()
                .allMatch(scope -> scope == PermissionScope.ALL));
    }
}
