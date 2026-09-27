package com.YM.Beverage.Distribution.Backend.user.services;

import com.YM.Beverage.Distribution.Backend.configs.security.JwtUtil;
import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreRepository;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.CreateUserDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.GetNewAccessTokenDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.LoginDTO;
import com.YM.Beverage.Distribution.Backend.user.models.RefreshToken;
import com.YM.Beverage.Distribution.Backend.user.models.Role;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.RefreshTokenRepository;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.CustomException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataAlreadyExistsException;
import com.YM.Beverage.Distribution.Backend.utils.exceptions.DataNotFoundException;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpService otpService;
    @Qualifier("customUserAuthenticationManager")
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final StoreRepository storeRepository;

    @Value("${refresh_token_validity}")
    private long refreshTokenValidity;


    @Transactional
    public ApiResponse register(CreateUserDTO createUserDTO) {
        String normalizedEmail = createUserDTO.getEmail().trim().toLowerCase(Locale.ROOT);
        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(normalizedEmail);

        if(existingUser.isPresent()){
            throw new DataAlreadyExistsException("User with this email already exists");
        }

        List<Role> roles = new ArrayList<>();

        if(createUserDTO.getRoleIds() != null){
            roles = roleRepository.findAllById(createUserDTO.getRoleIds());

            if(roles.size() != createUserDTO.getRoleIds().size()){
                throw new DataNotFoundException("One or more roles not found");
            }
        }

        String password = generatePassword();
        Store store = null;
        if (createUserDTO.getStoreId() != null) {
            store = storeRepository.findByIdAndActiveTrue(createUserDTO.getStoreId())
                    .orElseThrow(() -> new DataNotFoundException("Active store not found"));
        }

        User user = User.builder()
                .firstName(createUserDTO.getFirstName())
                .lastName(createUserDTO.getLastName())
                .phoneNumber(createUserDTO.getPhoneNumber())
                .email(normalizedEmail)
                .password(BCrypt.hashpw(password, BCrypt.gensalt()))
                .isDeactivated(false)
                .store(store)
                .roles(new HashSet<>(roles))
                .build();

        try {
            // Flush before sending the OTP so uniqueness failures are returned by this request,
            // rather than appearing after the email operation has started.
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new DataAlreadyExistsException("User with this email already exists");
        }

        otpService.generateOtp(user, password, false);

        return new ApiResponse("User registered successfully", HttpStatus.CREATED,
                Map.of("user", user.toUserResponseDTO()));
    }


    @Transactional
    public ApiResponse activateAccount(String email , String otp){
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
                ()-> new DataNotFoundException("User not found")
        );

        if(user.isActive()){
            return new ApiResponse("Your account is already activated",HttpStatus.BAD_REQUEST);
        }

        otpService.verifyOtp(user,otp,true);

        user.setActive(true);

        userRepository.save(user);
        return new ApiResponse("User account has been activated successfully",HttpStatus.OK);
    }

    @Transactional
    public ApiResponse login(LoginDTO loginDTO){
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));
            String email = authentication.getName();

            User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
                    () -> new DataNotFoundException("User not found")
            );

            if (!user.isActive()) {
                return new ApiResponse("Your account is not active yet", HttpStatus.BAD_REQUEST);
            }

            if (user.isDeactivated()) {
                return new ApiResponse("Your account is deactivated", HttpStatus.UNAUTHORIZED);
            }

            String accessToken = jwtUtil.createAccessToken(user);
            String refreshToken = jwtUtil.createRefreshToken(user.getId());
            storeRefreshToken(user.getId(),refreshToken);
            Map<String , Object > details = new HashMap<>();

            details.put("email", email);
            details.put("accessToken", accessToken);
            details.put("refreshToken", refreshToken);
            return new ApiResponse("Logged in successfully",HttpStatus.OK,details);
        }
        catch (BadCredentialsException e) {
            return new ApiResponse("Invalid phone or password",HttpStatus.BAD_REQUEST);
        }
    }

    public ApiResponse requestOtp(String email, Boolean isForPasswordReset){
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
                ()-> new DataNotFoundException("User not found")
        );

        otpService.generateOtp(user, "", isForPasswordReset == null || isForPasswordReset);
        return new ApiResponse("OTP has been successfully sent",HttpStatus.OK);
    }

    public ApiResponse verifyOtp(String email , String otp){
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
                ()-> new DataNotFoundException("User not found")
        );
        if(!user.isActive()){
            return new ApiResponse("Your account is not active yet",HttpStatus.BAD_REQUEST);
        }
        otpService.verifyOtp(user,otp,false);
        return new ApiResponse("Otp has been verified successfully",HttpStatus.OK);
    }

    public ApiResponse resetPassword(String email , String otp , String newPassword){
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
                ()-> new DataNotFoundException("User not found")
        );
        otpService.verifyOtp(user,otp,true);
        user.setPassword(BCrypt.hashpw(newPassword,BCrypt.gensalt()));
        userRepository.save(user);
        return new ApiResponse("Password changed successfully",HttpStatus.OK);
    }

    @Transactional
    public ApiResponse refreshAccessToken(GetNewAccessTokenDTO getNewAccessTokenDTO){
        UUID userId = validateRefreshToken(getNewAccessTokenDTO.getRefreshToken()).orElseThrow(
                ()-> new CustomException("Invalid refresh token",HttpStatus.UNAUTHORIZED,"")
        );

        User user = userRepository.findById(userId).orElseThrow(
                ()-> new DataNotFoundException("User not found")
        );
        if (!user.isActive()) {
            return new ApiResponse("Your account is deactivated",HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user.getId());
        storeRefreshToken(user.getId(),refreshToken);
        return new ApiResponse("Token refreshed successfully",HttpStatus.OK,Map.of(
                "accessToken",accessToken,
                "refreshToken",refreshToken
        ));
    }
    @Transactional
    public ApiResponse logout(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
        return new ApiResponse("Logged out successfully",HttpStatus.OK);
    }


    private void storeRefreshToken(UUID userId, String refreshToken) {
        refreshTokenRepository.deleteByUserId(userId);
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setToken(refreshToken);
        long expiryMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(refreshTokenValidity);
        token.setExpiryDate(expiryMillis);
        refreshTokenRepository.save(token);
    }

    private Optional<UUID> validateRefreshToken(String token) {
        if(!jwtUtil.validateToken(token)){
            throw new CustomException("Invalid refresh token",HttpStatus.UNAUTHORIZED,"");
        }
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        if (refreshTokenOpt.isEmpty() || refreshTokenOpt.get().getExpiryDate() < System.currentTimeMillis()) {
            return Optional.empty();
        }
        return Optional.of(refreshTokenOpt.get().getUserId());
    }

    private String generatePassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*";

        String allChars = upperCase + lowerCase + numbers + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Guarantee at least one character from each category
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Fill remaining characters (12 characters total)
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle so the first 4 characters aren't predictable by category
        List<Character> characters = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());

        Collections.shuffle(characters, random);

        return characters.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

}
