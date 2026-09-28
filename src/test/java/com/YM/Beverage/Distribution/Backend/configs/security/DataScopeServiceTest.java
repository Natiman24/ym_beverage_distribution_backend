package com.YM.Beverage.Distribution.Backend.configs.security;

import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataScopeServiceTest {

    @Mock private UserRepository userRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void storeUserCanOnlyAccessTheirOwnStore() {
        UUID ownStoreId = UUID.randomUUID();
        authenticate(User.builder()
                .email("store@example.com")
                .store(Store.builder().id(ownStoreId).build())
                .build());
        DataScopeService service = new DataScopeService(userRepository);

        assertEquals(ownStoreId, service.currentStoreId());
        assertFalse(service.isCompanyUser(SecurityContextHolder.getContext().getAuthentication()));
        assertDoesNotThrow(() -> service.assertCanAccessStore(ownStoreId));
        assertThrows(CustomException.class, () -> service.assertCanAccessStore(UUID.randomUUID()));
    }

    @Test
    void companyUserHasGlobalStoreScope() {
        authenticate(User.builder().email("company@example.com").store(null).build());
        DataScopeService service = new DataScopeService(userRepository);

        assertTrue(service.isCompanyUser(SecurityContextHolder.getContext().getAuthentication()));
        assertDoesNotThrow(() -> service.assertCanAccessStore(UUID.randomUUID()));
        assertDoesNotThrow(service::requireCompanyUser);
    }

    @Test
    void storeUserCannotAccessCompanyOrOtherStoreUsers() {
        UUID ownStoreId = UUID.randomUUID();
        authenticate(User.builder()
                .email("store@example.com")
                .store(Store.builder().id(ownStoreId).build())
                .build());
        DataScopeService service = new DataScopeService(userRepository);

        User companyUser = User.builder().store(null).build();
        User otherStoreUser = User.builder()
                .store(Store.builder().id(UUID.randomUUID()).build())
                .build();

        assertThrows(CustomException.class, () -> service.assertCanAccessUser(companyUser));
        assertThrows(CustomException.class, () -> service.assertCanAccessUser(otherStoreUser));
    }

    private void authenticate(User user) {
        String email = user.getEmail();
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }
}
