package com.YM.Beverage.Distribution.Backend.driver.repositories;

import com.YM.Beverage.Distribution.Backend.driver.models.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {
    @Query("SELECT COUNT(d) FROM DriverProfile d WHERE d.availableForDelivery = true AND d.user.isActive = true AND d.user.isDeactivated = false")
    long countAvailableDrivers();

    Optional<DriverProfile> findByLicenseNumber(String licenseNumber);

    Optional<DriverProfile> findByUserEmailIgnoreCase(String email);

    Optional<DriverProfile> findByUserPhoneNumber(String phoneNumber);
}
