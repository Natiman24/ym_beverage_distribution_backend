package com.YM.Beverage.Distribution.Backend.user.repositories;

import com.YM.Beverage.Distribution.Backend.user.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> , JpaSpecificationExecutor<User> {
    List<User> findByIdIn(List<UUID> ids, Sort sort);

    Page<User> findByIdIn(List<UUID> ids, Pageable pageable);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByEmailIn(List<String> emails);

    void deleteByEmail(String email);

    Optional<User> findByIdAndIsDeactivatedFalse(UUID userId);
}
