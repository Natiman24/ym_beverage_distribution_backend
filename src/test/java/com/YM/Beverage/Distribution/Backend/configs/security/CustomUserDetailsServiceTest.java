package com.YM.Beverage.Distribution.Backend.configs.security;

import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @Test
    void loadsDirectAndRolePermissionsAsAuthorities() {
        Role superAdmin = Role.builder()
                .name("Super Admin")
                .permissions(new HashSet<>(Set.of(Permission.ROLE_MANAGE, Permission.ORDER_APPROVE)))
                .build();
        User user = User.builder()
                .email("admin@example.com")
                .password("hash")
                .isActive(true)
                .isDeactivated(false)
                .roles(new HashSet<>(Set.of(superAdmin)))
                .permissions(new HashSet<>(Set.of(Permission.USER_VIEW)))
                .build();
        when(userRepository.findByEmailIgnoreCase("ADMIN@example.com")).thenReturn(Optional.of(user));

        UserDetails details = new CustomUserDetailsService(userRepository)
                .loadUserByUsername("ADMIN@example.com");

        Set<String> authorities = new HashSet<>();
        details.getAuthorities().forEach(authority -> authorities.add(authority.getAuthority()));
        assertTrue(authorities.contains("ROLE_MANAGE"));
        assertTrue(authorities.contains("ORDER_APPROVE"));
        assertTrue(authorities.contains("USER_VIEW"));
        assertTrue(authorities.contains("ROLE_SUPER_ADMIN"));
    }
}
