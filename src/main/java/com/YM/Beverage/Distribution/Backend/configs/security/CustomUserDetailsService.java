package com.YM.Beverage.Distribution.Backend.configs.security;

import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email)
        );

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        if (user.getPermissions() != null) {
            user.getPermissions().stream()
                    .map(Permission::name)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                if (role.getPermissions() != null) {
                    role.getPermissions().stream()
                            .map(Permission::name)
                            .map(SimpleGrantedAuthority::new)
                            .forEach(authorities::add);
                }

                if (role.getName() != null && !role.getName().isBlank()) {
                    String roleAuthority = "ROLE_" + role.getName().trim()
                            .toUpperCase(Locale.ROOT)
                            .replaceAll("[^A-Z0-9]+", "_");
                    authorities.add(new SimpleGrantedAuthority(roleAuthority));
                }
            }
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
