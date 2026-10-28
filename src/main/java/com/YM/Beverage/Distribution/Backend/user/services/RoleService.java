package com.YM.Beverage.Distribution.Backend.user.services;

import com.YM.Beverage.Distribution.Backend.user.dtos.role.CreateRoleDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.role.UpdateRoleDTO;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import com.YM.Beverage.Distribution.Backend.user.models.PermissionScope;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.user.specification.RoleSpecification;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataAlreadyExistsException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class RoleService {
    private static final String SUPER_ADMIN_ROLE = "Super Admin";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public ApiResponse getPermissions() {
        return new ApiResponse("", HttpStatus.OK, Map.of(
                "permissions", Arrays.stream(Permission.values()).map(Enum::name).toList(),
                "scopes", Arrays.stream(PermissionScope.values()).map(Enum::name).toList()));
    }

    public ApiResponse createRole(CreateRoleDTO dto) {
        Optional<Role> existing = roleRepository.findByNameIgnoreCase(dto.getName());
        if (existing.isPresent()) {
            throw new DataAlreadyExistsException("Role with this name already exists");
        }

        Set<Permission> permissions = dto.getPermissions() != null
                ? new HashSet<>(dto.getPermissions()) : new HashSet<>();
        Role role = Role.builder()
                .name(dto.getName())
                .permissions(permissions)
                .permissionScopes(normalizeScopes(permissions, dto.getPermissionScopes(), Map.of()))
                .build();

        roleRepository.save(role);

        return new ApiResponse("Role created successfully", HttpStatus.CREATED,
                Map.of("role", role.toResponseDTO()));
    }

    public ApiResponse getRoles(String searchQuery, Integer page, Integer pageSize) {
        Specification<Role> specification = new RoleSpecification(searchQuery);
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        if (page == null || pageSize == null) {
            List<Role> roles = roleRepository.findAll(specification, sort);
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("roles", roles.stream().map(Role::toResponseDTO).toList()));
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<Role> rolesPage = roleRepository.findAll(specification, pageable);

        return new ApiResponse("", HttpStatus.OK,
                Map.of("roles", rolesPage.getContent().stream().map(Role::toResponseDTO).toList(),
                        "pageSize", pageSize,
                        "currentPage", page,
                        "totalPages", rolesPage.getTotalPages(),
                        "totalElements", rolesPage.getTotalElements()));
    }

    public ApiResponse getRoleById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "role", role.toResponseDTO(),
                "userCount", userRepository.countByRoles_Id(role.getId())));
    }

    public ApiResponse updateRole(UUID id, UpdateRoleDTO dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        if (SUPER_ADMIN_ROLE.equalsIgnoreCase(role.getName())) {
            throw new CustomException("The Super Admin role cannot be edited", HttpStatus.FORBIDDEN, "protected_role");
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            Optional<Role> existingByName = roleRepository.findByNameIgnoreCase(dto.getName());
            if (existingByName.isPresent() && !existingByName.get().getId().equals(role.getId())) {
                throw new DataAlreadyExistsException("Role with this name already exists");
            }
            role.setName(dto.getName());
        }

        if (dto.getPermissions() != null) {
            Set<Permission> permissions = new HashSet<>(dto.getPermissions());
            role.setPermissions(permissions);
            role.setPermissionScopes(normalizeScopes(
                    permissions, dto.getPermissionScopes(), role.getPermissionScopes()));
        } else if (dto.getPermissionScopes() != null) {
            role.setPermissionScopes(normalizeScopes(
                    role.getPermissions(), dto.getPermissionScopes(), role.getPermissionScopes()));
        }

        roleRepository.save(role);

        return new ApiResponse("Role updated successfully", HttpStatus.OK,
                Map.of("role", role.toResponseDTO()));
    }

    public ApiResponse deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        if (SUPER_ADMIN_ROLE.equalsIgnoreCase(role.getName())) {
            throw new CustomException("The Super Admin role cannot be deleted", HttpStatus.FORBIDDEN, "protected_role");
        }

        roleRepository.delete(role);

        return new ApiResponse("Role deleted successfully", HttpStatus.OK);
    }

    private Map<Permission, PermissionScope> normalizeScopes(
            Set<Permission> permissions,
            Map<Permission, PermissionScope> requested,
            Map<Permission, PermissionScope> existing) {
        Map<Permission, PermissionScope> normalized = new HashMap<>();
        for (Permission permission : permissions) {
            PermissionScope scope = requested != null ? requested.get(permission) : null;
            if (scope == null && existing != null) scope = existing.get(permission);
            normalized.put(permission, scope != null ? scope : PermissionScope.ALL);
        }
        return normalized;
    }
}
