package com.YM.Beverage.Distribution.Backend.user.services;

import com.YM.Beverage.Distribution.Backend.configs.security.JwtUtil;
import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreRepository;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.CreateUserDTO;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.RefreshTokenRepository;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private OtpService otpService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private RoleRepository roleRepository;
    @Mock private StoreRepository storeRepository;

    @Test
    void registrationAssignsActiveStore() {
        UUID storeId = UUID.randomUUID();
        Store store = Store.builder().id(storeId).name("Airport Shop").active(true).build();
        CreateUserDTO dto = userDto(storeId);
        when(userRepository.findByEmailIgnoreCase(dto.getEmail())).thenReturn(Optional.empty());
        when(storeRepository.findByIdAndActiveTrue(storeId)).thenReturn(Optional.of(store));

        service().register(dto);

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(user.capture());
        assertEquals(storeId, user.getValue().getStore().getId());
    }

    @Test
    void registrationRejectsMissingOrInactiveStore() {
        UUID storeId = UUID.randomUUID();
        CreateUserDTO dto = userDto(storeId);
        when(userRepository.findByEmailIgnoreCase(dto.getEmail())).thenReturn(Optional.empty());
        when(storeRepository.findByIdAndActiveTrue(storeId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service().register(dto));

        verify(userRepository, never()).saveAndFlush(any());
        verify(otpService, never()).generateOtp(any(), any(), anyBoolean());
    }

    @Test
    void concurrentDuplicateEmailReturnsDomainConflictBeforeOtpIsSent() {
        CreateUserDTO dto = userDto(null);
        when(userRepository.findByEmailIgnoreCase(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        assertThrows(DataAlreadyExistsException.class, () -> service().register(dto));

        verify(otpService, never()).generateOtp(any(), any(), anyBoolean());
    }

    private AuthService service() {
        return new AuthService(userRepository, refreshTokenRepository, otpService,
                authenticationManager, jwtUtil, roleRepository, storeRepository);
    }

    private CreateUserDTO userDto(UUID storeId) {
        return CreateUserDTO.builder()
                .firstName("Store")
                .lastName("User")
                .phoneNumber("+251912345678")
                .email("store.user@example.com")
                .storeId(storeId)
                .build();
    }
}
