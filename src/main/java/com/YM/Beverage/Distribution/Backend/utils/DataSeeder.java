package com.YM.Beverage.Distribution.Backend.utils;

import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import com.YM.Beverage.Distribution.Backend.user.models.PermissionScope;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final String SUPER_ADMIN_ROLE = "Super Admin";

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedSuperAdminRole();
    }

    private void seedSuperAdminRole() {
        Set<Permission> allPermissions = new HashSet<>(Arrays.asList(Permission.values()));

        Role superAdmin = roleRepository.findByNameIgnoreCase(SUPER_ADMIN_ROLE)
                .orElseGet(() -> {
                    log.info("'{}' role not found. Creating it...", SUPER_ADMIN_ROLE);
                    return Role.builder().name(SUPER_ADMIN_ROLE).permissions(new HashSet<>()).build();
                });

        Set<Permission> missingPermissions = new HashSet<>(allPermissions);
        missingPermissions.removeAll(superAdmin.getPermissions());

        Map<Permission, PermissionScope> scopes = superAdmin.getPermissionScopes() != null
                ? new HashMap<>(superAdmin.getPermissionScopes()) : new HashMap<>();
        boolean scopesChanged = allPermissions.stream()
                .anyMatch(permission -> scopes.get(permission) != PermissionScope.ALL);

        if (!missingPermissions.isEmpty() || scopesChanged) {
            superAdmin.getPermissions().addAll(missingPermissions);
            allPermissions.forEach(permission -> scopes.put(permission, PermissionScope.ALL));
            superAdmin.setPermissionScopes(scopes);
            roleRepository.save(superAdmin);
            log.info("'{}' role synchronized with all permissions at ALL scope", SUPER_ADMIN_ROLE);
        } else {
            log.info("'{}' role already has all permissions. No changes needed.", SUPER_ADMIN_ROLE);
        }
    }
}
