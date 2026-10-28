package com.YM.Beverage.Distribution.Backend.driver.services;

import com.YM.Beverage.Distribution.Backend.driver.dtos.CreateDriverDTO;
import com.YM.Beverage.Distribution.Backend.driver.models.DriverProfile;
import com.YM.Beverage.Distribution.Backend.driver.repositories.DriverProfileRepository;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderRepository;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.user.services.OtpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock private DriverProfileRepository driverProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OtpService otpService;
    @Mock private BCryptPasswordEncoder passwordEncoder;

    @Test
    void addingDriverCapabilityPreservesSuperAdminRole() {
        UUID userId = UUID.randomUUID();
        Role superAdmin = Role.builder().id(UUID.randomUUID()).name("Super Admin").build();
        Role driverRole = Role.builder().id(UUID.randomUUID()).name("Driver").build();
        User user = User.builder()
                .id(userId)
                .firstName("Admin")
                .email("admin@example.com")
                .roles(Set.of(superAdmin))
                .build();

        when(roleRepository.findByNameIgnoreCase("DRIVER")).thenReturn(Optional.of(driverRole));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(driverProfileRepository.existsById(userId)).thenReturn(false);

        service().createDriver(CreateDriverDTO.builder().userId(userId).licenseNumber("LIC-1").build());

        assertTrue(user.getRoles().contains(superAdmin));
        assertTrue(user.getRoles().contains(driverRole));
        ArgumentCaptor<DriverProfile> profileCaptor = ArgumentCaptor.forClass(DriverProfile.class);
        verify(driverProfileRepository).save(profileCaptor.capture());
        assertTrue(profileCaptor.getValue().getUser() == user);
        assertTrue(profileCaptor.getValue().isAvailableForDelivery());
    }

    @Test
    void availabilityToggleDoesNotDeactivateTheUserAccount() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).isActive(true).isDeactivated(false).build();
        DriverProfile profile = DriverProfile.builder()
                .id(userId)
                .user(user)
                .availableForDelivery(true)
                .build();
        when(driverProfileRepository.findById(userId)).thenReturn(Optional.of(profile));

        service().toggleActiveStatus(userId);

        assertFalse(profile.isAvailableForDelivery());
        assertTrue(user.isActive());
        assertFalse(user.isDeactivated());
        verify(driverProfileRepository).save(profile);
    }

    private DriverService service() {
        return new DriverService(
                driverProfileRepository,
                userRepository,
                roleRepository,
                orderRepository,
                otpService,
                passwordEncoder);
    }
}
