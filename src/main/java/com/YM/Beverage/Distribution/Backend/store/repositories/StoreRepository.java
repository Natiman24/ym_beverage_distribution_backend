package com.YM.Beverage.Distribution.Backend.store.repositories;

import com.YM.Beverage.Distribution.Backend.store.models.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID>, JpaSpecificationExecutor<Store> {
    Optional<Store> findByNameIgnoreCase(String name);
    Optional<Store> findByPhoneNumber(String phoneNumber);
    Optional<Store> findByIdAndActiveTrue(UUID id);
}
