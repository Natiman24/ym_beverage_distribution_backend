package com.YM.Beverage.Distribution.Backend.user.repositories;

import com.YM.Beverage.Distribution.Backend.user.models.Otp;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findByUser(User user);
}
