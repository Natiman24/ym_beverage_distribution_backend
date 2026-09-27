package com.YM.Beverage.Distribution.Backend.supplier.repositories;

import com.YM.Beverage.Distribution.Backend.supplier.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID>, JpaSpecificationExecutor<Supplier> {
    Optional<Supplier> findByNameIgnoreCase(String name);
    Optional<Supplier> findByPhoneNumber(String phoneNumber);
}
