package com.YM.Beverage.Distribution.Backend.product.repositories;

import com.YM.Beverage.Distribution.Backend.product.models.VolumeUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface VolumeUnitRepository extends JpaRepository<VolumeUnit, UUID>, JpaSpecificationExecutor<VolumeUnit> {
    Optional<VolumeUnit> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
