package com.YM.Beverage.Distribution.Backend.user.services;

import com.YM.Beverage.Distribution.Backend.user.dtos.role.CreateRoleDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.role.UpdateRoleDTO;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import com.YM.Beverage.Distribution.Backend.user.specification.RoleSpecification;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataAlreadyExistsException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public ApiResponse getPermissions() {
        return new ApiResponse("", HttpStatus.OK, Map.of(
                "permissions", Arrays.stream(Permission.values()).map(Enum::name).toList()));
    }

    public ApiResponse createRole(CreateRoleDTO dto) {
        Optional<Role> existing = roleRepository.findByNameIgnoreCase(dto.getName());
        if (existing.isPresent()) {
            throw new DataAlreadyExistsException("Role with this name already exists");
        }

        Role role = Role.builder()
                .name(dto.getName())
                .permissions(dto.getPermissions() != null ? dto.getPermissions() : new java.util.HashSet<>())
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

        return new ApiResponse("", HttpStatus.OK, Map.of("role", role.toResponseDTO()));
    }

    public ApiResponse updateRole(UUID id, UpdateRoleDTO dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            Optional<Role> existingByName = roleRepository.findByNameIgnoreCase(dto.getName());
            if (existingByName.isPresent() && !existingByName.get().getId().equals(role.getId())) {
                throw new DataAlreadyExistsException("Role with this name already exists");
            }
            role.setName(dto.getName());
        }

        if (dto.getPermissions() != null) {
            role.setPermissions(dto.getPermissions());
        }

        roleRepository.save(role);

        return new ApiResponse("Role updated successfully", HttpStatus.OK,
                Map.of("role", role.toResponseDTO()));
    }

    public ApiResponse deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        roleRepository.delete(role);

        return new ApiResponse("Role deleted successfully", HttpStatus.OK);
    }
}
