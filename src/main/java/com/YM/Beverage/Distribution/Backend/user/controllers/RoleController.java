package com.YM.Beverage.Distribution.Backend.user.controllers;

import com.YM.Beverage.Distribution.Backend.user.dtos.role.CreateRoleDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.role.UpdateRoleDTO;
import com.YM.Beverage.Distribution.Backend.user.services.RoleService;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/role")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {
    private final RoleService roleService;

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse> getPermissions() {
        ApiResponse response = roleService.getPermissions();
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createRole(@Valid @RequestBody CreateRoleDTO createRoleDTO) {
        ApiResponse response = roleService.createRole(createRoleDTO);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getRoles(
            @RequestParam(value = "search-query", required = false) String searchQuery,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize) {
        ApiResponse response = roleService.getRoles(searchQuery, page, pageSize);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getRoleById(@PathVariable UUID id) {
        ApiResponse response = roleService.getRoleById(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleDTO updateRoleDTO) {
        ApiResponse response = roleService.updateRole(id, updateRoleDTO);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteRole(@PathVariable UUID id) {
        ApiResponse response = roleService.deleteRole(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }
}
