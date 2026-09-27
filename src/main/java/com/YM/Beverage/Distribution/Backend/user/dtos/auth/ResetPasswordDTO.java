package com.YM.Beverage.Distribution.Backend.user.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordDTO {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Otp is required")
    private String otp;

    @NotBlank(message = "New password is required")
    private String newPassword;
}
