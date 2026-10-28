package com.YM.Beverage.Distribution.Backend.configs.security;

import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import com.YM.Beverage.Distribution.Backend.user.models.PermissionScope;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("dataScope")
@RequiredArgsConstructor
public class DataScopeService {

    private final UserRepository userRepository;

    public boolean isCompanyUser(Authentication authentication) {
        return maximumScope(authenticatedUser(authentication)) == PermissionScope.ALL;
    }

    public UUID currentStoreId() {
        User user = currentUser();
        return user.getStore() == null ? null : user.getStore().getId();
    }

    public UUID currentUserId() {
        return currentUser().getId();
    }

    public boolean isCurrentUserCompanyUser() {
        return maximumScope(currentUser()) == PermissionScope.ALL;
    }

    public PermissionScope currentPermissionScope(Permission permission) {
        User user = currentUser();
        PermissionScope effective = null;

        if (user.getPermissions() != null && user.getPermissions().contains(permission)) {
            effective = scopeFor(user.getPermissionScopes(), permission);
        }

        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                if (role.getPermissions() != null && role.getPermissions().contains(permission)) {
                    PermissionScope roleScope = scopeFor(role.getPermissionScopes(), permission);
                    effective = effective == null ? roleScope : effective.broaderOf(roleScope);
                }
            }
        }

        if (effective == null) {
            throw new CustomException(
                    "You do not have the required permission",
                    HttpStatus.FORBIDDEN,
                    "permission_not_granted");
        }

        return effective.limitedTo(maximumScope(user));
    }

    public boolean hasPermission(Permission permission) {
        User user = currentUser();
        if (user.getPermissions() != null && user.getPermissions().contains(permission)) {
            return true;
        }
        return user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> role.getPermissions() != null && role.getPermissions().contains(permission));
    }

    public void requireCompanyUser() {
        if (!isCurrentUserCompanyUser()) {
            throw new CustomException(
                    "This operation is restricted to beverage company users",
                    HttpStatus.FORBIDDEN,
                    "company_user_required");
        }
    }

    public void assertCanAccessStore(UUID storeId) {
        UUID currentStoreId = currentStoreId();
        if (currentStoreId != null && !currentStoreId.equals(storeId)) {
            throw forbidden();
        }
    }

    public void assertCanAccessUser(User targetUser) {
        UUID currentStoreId = currentStoreId();
        if (currentStoreId == null) {
            return;
        }
        if (targetUser.getStore() == null || !currentStoreId.equals(targetUser.getStore().getId())) {
            throw forbidden();
        }
    }

    private User currentUser() {
        return authenticatedUser(SecurityContextHolder.getContext().getAuthentication());
    }

    private User authenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw forbidden();
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new DataNotFoundException("Authenticated user not found"));
    }

    private PermissionScope scopeFor(
            java.util.Map<Permission, PermissionScope> scopes,
            Permission permission) {
        if (scopes == null) return PermissionScope.ALL;
        return scopes.getOrDefault(permission, PermissionScope.ALL);
    }

    private PermissionScope maximumScope(User user) {
        if (user.getStore() != null) return PermissionScope.STORE;
        return PermissionScope.ALL;
    }

    private CustomException forbidden() {
        return new CustomException(
                "You are not authorized to access data outside your store",
                HttpStatus.FORBIDDEN,
                "store_scope_violation");
    }
}
