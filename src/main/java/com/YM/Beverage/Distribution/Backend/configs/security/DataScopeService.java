package com.YM.Beverage.Distribution.Backend.configs.security;

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
        return authenticatedUser(authentication).getStore() == null;
    }

    public UUID currentStoreId() {
        User user = currentUser();
        return user.getStore() == null ? null : user.getStore().getId();
    }

    public UUID currentUserId() {
        return currentUser().getId();
    }

    public boolean isCurrentUserCompanyUser() {
        return currentUser().getStore() == null;
    }

    public void requireCompanyUser() {
        if (currentStoreId() != null) {
            throw forbidden();
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

    private CustomException forbidden() {
        return new CustomException(
                "You are not authorized to access data outside your store",
                HttpStatus.FORBIDDEN,
                "store_scope_violation");
    }
}
