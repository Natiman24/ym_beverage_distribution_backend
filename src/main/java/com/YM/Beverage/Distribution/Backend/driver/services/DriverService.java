package com.YM.Beverage.Distribution.Backend.driver.services;

import com.YM.Beverage.Distribution.Backend.driver.dtos.CreateDriverDTO;
import com.YM.Beverage.Distribution.Backend.driver.dtos.DriverResponseDTO;
import com.YM.Beverage.Distribution.Backend.driver.dtos.UpdateDriverDTO;
import com.YM.Beverage.Distribution.Backend.driver.models.DriverProfile;
import com.YM.Beverage.Distribution.Backend.driver.repositories.DriverProfileRepository;
import com.YM.Beverage.Distribution.Backend.order.repositories.OrderRepository;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.user.services.OtpService;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataAlreadyExistsException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final OtpService otpService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponse createDriver(CreateDriverDTO dto) {
        Role driverRole = roleRepository.findByNameIgnoreCase("DRIVER")
                .orElseThrow(() -> new DataNotFoundException("DRIVER role not found. Please create it first."));

        boolean existingAccount = dto.getUserId() != null;
        User user;
        if (existingAccount) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new DataNotFoundException("User not found"));
            if (user.getStore() != null) {
                throw new CustomException(
                        "Only beverage company users can be given driver capability",
                        HttpStatus.BAD_REQUEST,
                        "company_user_required");
            }
            if (driverProfileRepository.existsById(user.getId())) {
                throw new DataAlreadyExistsException("This user already has a driver profile");
            }
        } else {
            validateNewUser(dto);
            String normalizedEmail = dto.getEmail().trim().toLowerCase(Locale.ROOT);
            if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
                throw new DataAlreadyExistsException("A user with this email already exists");
            }
            if (userRepository.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
                throw new DataAlreadyExistsException("A user with this phone number already exists");
            }

            String password = generatePassword();
            user = User.builder()
                    .firstName(dto.getFirstName().trim())
                    .lastName(dto.getLastName())
                    .phoneNumber(dto.getPhoneNumber())
                    .email(normalizedEmail)
                    .password(passwordEncoder.encode(password))
                    .isActive(true)
                    .isDeactivated(false)
                    .roles(new HashSet<>())
                    .build();
            addDriverRole(user, driverRole);
            userRepository.saveAndFlush(user);
            otpService.generateOtp(user, password, false);
        }

        ensureUniqueLicense(dto.getLicenseNumber(), null);
        addDriverRole(user, driverRole);
        userRepository.save(user);

        DriverProfile profile = DriverProfile.builder()
                .user(user)
                .licenseNumber(blankToNull(dto.getLicenseNumber()))
                .availableForDelivery(true)
                .build();
        driverProfileRepository.save(profile);

        String message = existingAccount
                ? "Driver capability added to user successfully"
                : "Driver created successfully";
        return new ApiResponse(message, HttpStatus.CREATED,
                Map.of("driver", profile.toResponseDTO()));
    }

    public ApiResponse getDrivers(String searchQuery, Boolean active, Boolean isDeactivated,
                                  Integer page, Integer pageSize) {
        String search = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        List<DriverProfile> all = driverProfileRepository.findAll().stream()
                .filter(profile -> matchesStatus(profile, active, isDeactivated))
                .filter(profile -> matchesSearch(profile, search))
                .sorted(Comparator.comparing((DriverProfile profile) -> profile.getUser().getFirstName(),
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(profile -> profile.getUser().getLastName(),
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();

        if (page == null || pageSize == null) {
            return new ApiResponse("", HttpStatus.OK,
                    Map.of("drivers", all.stream().map(DriverProfile::toResponseDTO).toList()));
        }
        if (page < 1 || pageSize < 1) {
            return new ApiResponse("Page and pageSize must be greater than 0", HttpStatus.BAD_REQUEST);
        }

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, all.size());
        List<DriverResponseDTO> result = start >= all.size()
                ? List.of()
                : all.subList(start, end).stream().map(DriverProfile::toResponseDTO).toList();
        int totalPages = (int) Math.ceil((double) all.size() / pageSize);

        return new ApiResponse("", HttpStatus.OK, Map.of(
                "drivers", result,
                "pageSize", pageSize,
                "currentPage", page,
                "totalPages", totalPages,
                "totalElements", all.size()));
    }

    public ApiResponse getDriverCandidates() {
        Set<UUID> profileUserIds = driverProfileRepository.findAll().stream()
                .map(DriverProfile::getId)
                .collect(Collectors.toSet());
        List<?> candidates = userRepository.findAll(Sort.by("firstName", "lastName")).stream()
                .filter(user -> user.getStore() == null)
                .filter(user -> !profileUserIds.contains(user.getId()))
                .map(User::toUserListResponseDTO)
                .toList();
        return new ApiResponse("", HttpStatus.OK, Map.of("users", candidates));
    }

    public ApiResponse getDriverById(UUID id) {
        DriverProfile profile = findProfile(id);
        return new ApiResponse("", HttpStatus.OK, Map.of("driver", profile.toResponseDTO()));
    }

    public ApiResponse getMyProfile(UUID userId) {
        DriverProfile profile = findProfile(userId);
        return new ApiResponse("", HttpStatus.OK, Map.of("driver", profile.toResponseDTO()));
    }

    @Transactional
    public ApiResponse updateDriver(UUID id, UpdateDriverDTO dto) {
        DriverProfile profile = findProfile(id);
        User user = profile.getUser();

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().equals(user.getPhoneNumber())) {
            userRepository.findByPhoneNumber(dto.getPhoneNumber()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new DataAlreadyExistsException("A user with this phone number already exists");
                }
            });
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            String normalizedEmail = dto.getEmail().trim().toLowerCase(Locale.ROOT);
            userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new DataAlreadyExistsException("A user with this email already exists");
                }
            });
            user.setEmail(normalizedEmail);
        }
        if (dto.getLicenseNumber() != null && !dto.getLicenseNumber().equals(profile.getLicenseNumber())) {
            ensureUniqueLicense(dto.getLicenseNumber(), profile.getId());
            profile.setLicenseNumber(blankToNull(dto.getLicenseNumber()));
        }
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) user.setFirstName(dto.getFirstName().trim());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName().trim());

        userRepository.save(user);
        driverProfileRepository.save(profile);
        return new ApiResponse("Driver updated successfully", HttpStatus.OK,
                Map.of("driver", profile.toResponseDTO()));
    }

    @Transactional
    public ApiResponse toggleActiveStatus(UUID id) {
        DriverProfile profile = findProfile(id);
        profile.setAvailableForDelivery(!profile.isAvailableForDelivery());
        driverProfileRepository.save(profile);
        String message = profile.isAvailableForDelivery()
                ? "Driver is now available for delivery"
                : "Driver is no longer available for delivery";
        return new ApiResponse(message, HttpStatus.OK, Map.of("driver", profile.toResponseDTO()));
    }

    @Transactional
    public ApiResponse deleteDriver(UUID id) {
        DriverProfile profile = findProfile(id);
        if (orderRepository.existsByDriverId(id)) {
            throw new CustomException(
                    "This driver has order history and cannot be removed. Mark them unavailable instead.",
                    HttpStatus.CONFLICT,
                    "driver_has_orders");
        }

        User user = profile.getUser();
        driverProfileRepository.delete(profile);
        if (user.getRoles() != null) {
            user.setRoles(user.getRoles().stream()
                    .filter(role -> !"Driver".equalsIgnoreCase(role.getName()))
                    .collect(Collectors.toCollection(HashSet::new)));
            userRepository.save(user);
        }
        return new ApiResponse("Driver capability removed successfully", HttpStatus.OK);
    }

    private DriverProfile findProfile(UUID id) {
        return driverProfileRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Driver profile not found"));
    }

    private void addDriverRole(User user, Role driverRole) {
        Set<Role> roles = user.getRoles() == null ? new HashSet<>() : new HashSet<>(user.getRoles());
        roles.add(driverRole);
        user.setRoles(roles);
    }

    private void validateNewUser(CreateDriverDTO dto) {
        List<String> missing = new ArrayList<>();
        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) missing.add("first name");
        if (dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank()) missing.add("phone number");
        if (dto.getEmail() == null || dto.getEmail().isBlank()) missing.add("email");
        if (!missing.isEmpty()) {
            throw new CustomException(
                    "New driver accounts require " + String.join(", ", missing),
                    HttpStatus.BAD_REQUEST,
                    "missing_driver_account_fields");
        }
    }

    private void ensureUniqueLicense(String licenseNumber, UUID currentProfileId) {
        String normalized = blankToNull(licenseNumber);
        if (normalized == null) return;
        driverProfileRepository.findByLicenseNumber(normalized).ifPresent(existing -> {
            if (currentProfileId == null || !existing.getId().equals(currentProfileId)) {
                throw new DataAlreadyExistsException("A driver with this license number already exists");
            }
        });
    }

    private boolean matchesStatus(DriverProfile profile, Boolean active, Boolean isDeactivated) {
        User user = profile.getUser();
        boolean operational = profile.isAvailableForDelivery() && user.isActive() && !user.isDeactivated();
        return (active == null || operational == active)
                && (isDeactivated == null || user.isDeactivated() == isDeactivated);
    }

    private boolean matchesSearch(DriverProfile profile, String search) {
        if (search.isEmpty()) return true;
        User user = profile.getUser();
        return contains(user.getFirstName(), search)
                || contains(user.getLastName(), search)
                || contains(user.getEmail(), search)
                || contains(user.getPhoneNumber(), search)
                || contains(profile.getLicenseNumber(), search);
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String generatePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String all = upper + lower + digits + special;
        SecureRandom random = new SecureRandom();
        StringBuilder result = new StringBuilder();
        result.append(upper.charAt(random.nextInt(upper.length())));
        result.append(lower.charAt(random.nextInt(lower.length())));
        result.append(digits.charAt(random.nextInt(digits.length())));
        result.append(special.charAt(random.nextInt(special.length())));
        for (int index = 4; index < 12; index++) result.append(all.charAt(random.nextInt(all.length())));
        List<Character> characters = result.chars().mapToObj(value -> (char) value).collect(Collectors.toList());
        Collections.shuffle(characters, random);
        return characters.stream().map(String::valueOf).collect(Collectors.joining());
    }
}
