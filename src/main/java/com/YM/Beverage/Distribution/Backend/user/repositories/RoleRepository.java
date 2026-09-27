package com.YM.Beverage.Distribution.Backend.user.repositories;

import com.YM.Beverage.Distribution.Backend.user.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID>, JpaSpecificationExecutor<Role> {
    Optional<Role> findByNameIgnoreCase(String name);
}
