package com.YM.Beverage.Distribution.Backend.user.controllers;

import com.YM.Beverage.Distribution.Backend.configs.security.JwtUtil;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.ChangePasswordDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.EditProfileDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.UpdateUserRolesPermissionsDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.UpdateUserStoreDTO;
import com.YM.Beverage.Distribution.Backend.user.services.UserService;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest httpServletRequest;


    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyProfile() {
        UUID userId = jwtUtil.getUserId(jwtUtil.resolveToken(httpServletRequest));
        ApiResponse response = userService.getProfile(userId);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/edit-profile")
    public ResponseEntity<ApiResponse> editProfile(@Valid @ModelAttribute EditProfileDTO editProfileDTO){
        UUID userId = jwtUtil.getUserId(jwtUtil.resolveToken(httpServletRequest));
        ApiResponse response = userService.editProfile(userId, editProfileDTO);
        return new ResponseEntity<>(response, response.getStatusCode());
    }


    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        UUID userId = jwtUtil.getUserId(jwtUtil.resolveToken(httpServletRequest));
        ApiResponse response = userService.changePassword(userId,changePasswordDTO);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable("id") UUID id) {
        ApiResponse response = userService.getUserById(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/change-activation-status")
    public ResponseEntity<ApiResponse> changeActivationStatus(@PathVariable UUID id){
        ApiResponse response = userService.changeActivationStatus(id);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    @PatchMapping("/{id}/roles-permissions")
    public ResponseEntity<ApiResponse> updateUserRoles(
            @PathVariable("id") UUID id,
            @RequestBody UpdateUserRolesPermissionsDTO dto) {
        ApiResponse response = userService.updateUserRolesandPermissions(id, dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PatchMapping("/{id}/store")
    public ResponseEntity<ApiResponse> updateUserStore(
            @PathVariable("id") UUID id,
            @RequestBody UpdateUserStoreDTO dto) {
        ApiResponse response = userService.updateUserStore(id, dto);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUsers(
            @RequestParam(value = "search-query", required = false) String searchQuery,
            @RequestParam(value = "is-active", required = false) Boolean isActive,
            @RequestParam(value = "is-deactivated", required = false) Boolean isDeactivated,
            @RequestParam(value = "roles", required = false) List<UUID> roles,
            @RequestParam(value = "store-id", required = false) UUID storeId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "page-size", required = false) Integer pageSize
    ) {
        ApiResponse response = userService.getUsers(
                searchQuery, isActive, isDeactivated, roles, storeId, page, pageSize);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

}
