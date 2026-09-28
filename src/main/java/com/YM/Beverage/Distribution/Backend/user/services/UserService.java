package com.YM.Beverage.Distribution.Backend.user.services;

import com.YM.Beverage.Distribution.Backend.configs.security.DataScopeService;
import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreRepository;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.ChangePasswordDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.EditProfileDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.UpdateUserRolesPermissionsDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.UpdateUserStoreDTO;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.user.specification.UserSpecification;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StoreRepository storeRepository;
    private final DataScopeService dataScopeService;

    public ApiResponse getProfile(UUID userId) {

        User user = userRepository.findById(userId).orElseThrow(
                ()-> new DataNotFoundException("User not found")
        );

        return new ApiResponse("",HttpStatus.OK,Map.of("user", user.toUserResponseDTO()));
    }

    @Transactional
    public ApiResponse editProfile(UUID userId , EditProfileDTO editProfileDTO) {
        User user = userRepository.findById(userId).orElseThrow(
                ()-> new DataNotFoundException("user doesn't exist")
        );

        if(editProfileDTO.getFirstName() != null){
            user.setFirstName(editProfileDTO.getFirstName().trim());
        }

        if(editProfileDTO.getLastName() != null){
            user.setLastName(editProfileDTO.getLastName().trim());
        }

        if(editProfileDTO.getPhoneNumber() != null){
            verifyPhoneNumber(editProfileDTO.getPhoneNumber().trim());
            user.setPhoneNumber(editProfileDTO.getPhoneNumber().trim());
        }

        userRepository.save(user);
        return new ApiResponse("User profile edited successfully", HttpStatus.OK, Map.of("user", user.toUserResponseDTO()));
    }

    public ApiResponse getUsers(String searchQuery, Boolean isActive, Boolean isDeactivated,
                                List<UUID> roles, UUID storeId,
                                Integer page, Integer pageSize){
        Sort sort = Sort.by("createdAt").descending();

        UUID requesterStoreId = dataScopeService.currentStoreId();
        if (requesterStoreId != null) {
            storeId = requesterStoreId;
        }

        Specification<User> spec = new UserSpecification(
                searchQuery,
                isActive,
                isDeactivated,
                roles,
                storeId
        );

        if (page == null || pageSize == null) {
            List<User> users = userRepository.findAll(spec, sort);
            return new ApiResponse("", HttpStatus.OK, Map.of("users", users.stream().map(User::toUserListResponseDTO).toList()));
        } else {
            if(page < 1 || pageSize < 1){
                return new ApiResponse("Page and pageSize must be greater than 0", HttpStatus.BAD_REQUEST);
            }

            Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
            Page<User> userPage = userRepository.findAll(spec, pageable);

            return new ApiResponse("", HttpStatus.OK, Map.of(
                    "users", userPage.getContent().stream().map(User::toUserListResponseDTO).toList(),
                    "totalPages", userPage.getTotalPages(),
                    "totalElements", userPage.getTotalElements(),
                    "currentPage", page,
                    "pageSize", pageSize
            ));
        }
    }

    public ApiResponse changePassword(UUID userId , ChangePasswordDTO changePasswordDTO) throws DataNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException("User not found")
        );

        if(changePasswordDTO.getOldPassword().equals(changePasswordDTO.getNewPassword())){
            return new ApiResponse("New password cannot be the same as old password", HttpStatus.BAD_REQUEST);
        }

        if(!user.isActive()){
            return new ApiResponse("User account is not active", HttpStatus.BAD_REQUEST);
        }

        if(!BCrypt.checkpw(changePasswordDTO.getOldPassword(),user.getPassword())){
            return new ApiResponse("Old password is not correct", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(BCrypt.hashpw(changePasswordDTO.getNewPassword(),BCrypt.gensalt()));
        userRepository.save(user);
        return new ApiResponse("Password changed successfully", HttpStatus.OK);
    }

    public ApiResponse getUserById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException("User not found")
        );
        dataScopeService.assertCanAccessUser(user);
        return new ApiResponse("", HttpStatus.OK, Map.of("user", user.toUserResponseDTO()));
    }

    protected void verifyPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("^\\+251[0-9]{9}$")) {
            throw new CustomException("Invalid phone number format", HttpStatus.BAD_REQUEST, "invalid_phone_number");
        }
    }

    @Transactional
    public ApiResponse updateUserRolesandPermissions(UUID userId, UpdateUserRolesPermissionsDTO dto) {
        dataScopeService.requireCompanyUser();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        if (dto.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            if (roles.size() != dto.getRoleIds().size()) {
                throw new DataNotFoundException("One or more roles not found");
            }
            user.setRoles(roles);
        }

        if (dto.getPermissions() != null) {
            user.setPermissions(dto.getPermissions());
        }

        userRepository.save(user);

        return new ApiResponse("User roles and permissions updated successfully", HttpStatus.OK, Map.of("user", user.toUserResponseDTO()));
    }

    @Transactional
    public ApiResponse editUser(UUID userId, EditProfileDTO editProfileDTO) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException("User not found")
        );
        dataScopeService.assertCanAccessUser(user);

        if (editProfileDTO.getFirstName() != null && !editProfileDTO.getFirstName().isBlank()) {
            user.setFirstName(editProfileDTO.getFirstName().trim());
        }
        if (editProfileDTO.getLastName() != null) {
            user.setLastName(editProfileDTO.getLastName().trim());
        }
        if (editProfileDTO.getPhoneNumber() != null) {
            verifyPhoneNumber(editProfileDTO.getPhoneNumber().trim());
            user.setPhoneNumber(editProfileDTO.getPhoneNumber().trim());
        }

        userRepository.save(user);
        return new ApiResponse("User updated successfully", HttpStatus.OK,
                Map.of("user", user.toUserResponseDTO()));
    }

    @Transactional
    public ApiResponse updateUserStore(UUID userId, UpdateUserStoreDTO dto) {
        dataScopeService.requireCompanyUser();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        Store store = null;
        if (dto.getStoreId() != null) {
            store = storeRepository.findByIdAndActiveTrue(dto.getStoreId())
                    .orElseThrow(() -> new DataNotFoundException("Active store not found"));
        }

        user.setStore(store);
        userRepository.save(user);

        String message = store == null
                ? "User store assignment removed successfully"
                : "User store updated successfully";
        return new ApiResponse(message, HttpStatus.OK, Map.of("user", user.toUserResponseDTO()));
    }

    public ApiResponse changeActivationStatus(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        dataScopeService.assertCanAccessUser(user);

        String message;
        if (user.isDeactivated()) {
            user.setDeactivated(false);
            message = "User activated successfully";
        } else {
            user.setDeactivated(true);
            message = "User deactivated successfully";
        }

        userRepository.save(user);

        return new ApiResponse(message, HttpStatus.OK,
                Map.of("user", user.toUserResponseDTO()));
    }

}
