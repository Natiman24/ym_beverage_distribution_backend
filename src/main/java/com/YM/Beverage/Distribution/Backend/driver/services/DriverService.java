package com.YM.Beverage.Distribution.Backend.driver.services;

import com.YM.Beverage.Distribution.Backend.driver.dtos.CreateDriverDTO;
import com.YM.Beverage.Distribution.Backend.driver.dtos.DriverResponseDTO;
import com.YM.Beverage.Distribution.Backend.driver.dtos.UpdateDriverDTO;
import com.YM.Beverage.Distribution.Backend.driver.models.Driver;
import com.YM.Beverage.Distribution.Backend.driver.repositories.DriverRepository;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import com.YM.Beverage.Distribution.Backend.user.services.OtpService;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataAlreadyExistsException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final RoleRepository roleRepository;
    private final OtpService otpService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponse createDriver(CreateDriverDTO dto) {
        if (driverRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DataAlreadyExistsException("A driver with this email already exists");
        }
        if (driverRepository.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            throw new DataAlreadyExistsException("A driver with this phone number already exists");
        }
        if (dto.getLicenseNumber() != null && driverRepository.findByLicenseNumber(dto.getLicenseNumber()).isPresent()) {
            throw new DataAlreadyExistsException("A driver with this license number already exists");
        }

        Role driverRole = roleRepository.findByNameIgnoreCase("DRIVER")
                .orElseThrow(() -> new DataNotFoundException("DRIVER role not found. Please create it first."));

        String password = generatePassword();

        Driver driver = Driver.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(password))
                .isActive(false)
                .isDeactivated(false)
                .roles(new HashSet<>(Set.of(driverRole)))
                .licenseNumber(dto.getLicenseNumber())
                .build();

        driverRepository.save(driver);
        otpService.generateOtp(driver, password, false);

        return new ApiResponse("Driver created successfully", HttpStatus.CREATED,
                Map.of("driver", driver.toResponseDTO()));
    }

    public ApiResponse getDrivers(String searchQuery, Boolean active, Boolean isDeactivated,
                                  Integer page, Integer pageSize) {
        Sort sort = Sort.by(Sort.Direction.ASC, "firstName", "lastName");

        String normalizedSearch = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        List<Driver> all = driverRepository.findAll(sort).stream()
                .filter(driver -> active == null || driver.isActive() == active)
                .filter(driver -> isDeactivated == null || driver.isDeactivated() == isDeactivated)
                .filter(driver -> normalizedSearch.isEmpty()
                        || containsIgnoreCase(driver.getFirstName(), normalizedSearch)
                        || containsIgnoreCase(driver.getLastName(), normalizedSearch)
                        || containsIgnoreCase(driver.getEmail(), normalizedSearch)
                        || containsIgnoreCase(driver.getPhoneNumber(), normalizedSearch)
                        || containsIgnoreCase(driver.getLicenseNumber(), normalizedSearch))
                .toList();

        if (page == null || pageSize == null) {
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("drivers", all.stream().map(Driver::toResponseDTO).toList()));
        }

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, all.size());
        List<DriverResponseDTO> paged = (start >= all.size()) ? List.of()
                : all.subList(start, end).stream().map(Driver::toResponseDTO).toList();
        int totalPages = (int) Math.ceil((double) all.size() / pageSize);

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "drivers", paged,
                "pageSize", pageSize,
                "currentPage", page,
                "totalPages", totalPages,
                "totalElements", all.size()));
    }

    private boolean containsIgnoreCase(String value, String normalizedSearch) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedSearch);
    }

    public ApiResponse getDriverById(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Driver not found"));
        return new ApiResponse("", HttpStatus.OK, Map.of("driver", driver.toResponseDTO()));
    }

    public ApiResponse getMyProfile(UUID driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DataNotFoundException("Driver not found"));
        return new ApiResponse("", HttpStatus.OK, Map.of("driver", driver.toResponseDTO()));
    }

    @Transactional
    public ApiResponse updateDriver(UUID id, UpdateDriverDTO dto) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Driver not found"));

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().equals(driver.getPhoneNumber())) {
            if (driverRepository.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
                throw new DataAlreadyExistsException("A driver with this phone number already exists");
            }
            driver.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(driver.getEmail())) {
            if (driverRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new DataAlreadyExistsException("A driver with this email already exists");
            }
            driver.setEmail(dto.getEmail());
        }
        if (dto.getLicenseNumber() != null && !dto.getLicenseNumber().equals(driver.getLicenseNumber())) {
            if (driverRepository.findByLicenseNumber(dto.getLicenseNumber()).isPresent()) {
                throw new DataAlreadyExistsException("A driver with this license number already exists");
            }
            driver.setLicenseNumber(dto.getLicenseNumber());
        }
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) driver.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) driver.setLastName(dto.getLastName());

        driverRepository.save(driver);
        return new ApiResponse("Driver updated successfully", HttpStatus.OK,
                Map.of("driver", driver.toResponseDTO()));
    }

    @Transactional
    public ApiResponse toggleActiveStatus(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Driver not found"));
        driver.setDeactivated(!driver.isDeactivated());
        driverRepository.save(driver);
        String msg = !driver.isDeactivated() ? "Driver activated successfully" : "Driver deactivated successfully";
        return new ApiResponse(msg, HttpStatus.OK, Map.of("driver", driver.toResponseDTO()));
    }

    @Transactional
    public ApiResponse deleteDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Driver not found"));
        driverRepository.delete(driver);
        return new ApiResponse("Driver deleted successfully", HttpStatus.OK);
    }

    private String generatePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String all = upper + lower + digits + special;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));
        for (int i = 4; i < 12; i++) sb.append(all.charAt(random.nextInt(all.length())));
        List<Character> chars = sb.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(chars, random);
        return chars.stream().map(String::valueOf).collect(Collectors.joining());
    }
}
