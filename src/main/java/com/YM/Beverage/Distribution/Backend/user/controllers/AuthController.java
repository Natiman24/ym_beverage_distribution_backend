package com.YM.Beverage.Distribution.Backend.user.controllers;

import com.YM.Beverage.Distribution.Backend.configs.security.JwtUtil;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.CreateUserDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.GetNewAccessTokenDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.LoginDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.OtpVerificationDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.RequestOtpDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.auth.ResetPasswordDTO;
import com.YM.Beverage.Distribution.Backend.user.services.AuthService;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest httpServletRequest;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody CreateUserDTO createUserDTO) {
        ApiResponse response = authService.register(createUserDTO);
        return new ResponseEntity<>(response,response.getStatusCode());
    }

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse> activateAccount(@RequestBody @Valid OtpVerificationDTO dto) {
        ApiResponse response = authService.activateAccount(dto.getEmail(), dto.getOtp());
        return new ResponseEntity<>(response,response.getStatusCode());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginDTO loginDTO) {
        ApiResponse response = authService.login(loginDTO);
        return new ResponseEntity<>(response,response.getStatusCode());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshAccessToken(@RequestBody @Valid GetNewAccessTokenDTO getNewAccessTokenDTO) {
        ApiResponse response = authService.refreshAccessToken(getNewAccessTokenDTO);
        return new ResponseEntity<>(response,response.getStatusCode());
    }

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse> requestOtp(@RequestBody @Valid RequestOtpDTO dto) {
        ApiResponse response = authService.requestOtp(dto.getEmail(), dto.getIsForPasswordReset());
        return new ResponseEntity<>(response,response.getStatusCode());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody @Valid OtpVerificationDTO dto) {
        ApiResponse response = authService.verifyOtp(dto.getEmail(), dto.getOtp());
        return new ResponseEntity<>(response,response.getStatusCode());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody @Valid ResetPasswordDTO resetPasswordDTO) {
        ApiResponse response = authService.resetPassword(resetPasswordDTO.getEmail(), resetPasswordDTO.getOtp(), resetPasswordDTO.getNewPassword());
        return new ResponseEntity<>(response,response.getStatusCode());
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        UUID userId = jwtUtil.getUserId(jwtUtil.resolveToken(httpServletRequest));
        if(userId == null){
            throw new SecurityException("Access token not found in authorization header");
        }
        ApiResponse response = authService.logout(userId);
        return new ResponseEntity<>(response,response.getStatusCode());
    }


}
