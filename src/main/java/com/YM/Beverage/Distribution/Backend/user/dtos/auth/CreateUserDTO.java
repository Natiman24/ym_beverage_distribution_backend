package com.YM.Beverage.Distribution.Backend.user.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDTO {
    @NotBlank(message = "First name is required.")
    private String firstName;

    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+251[0-9]{9}$", message = "Phone number must start with +251 and contain exactly 9 digits after that.")
    private String phoneNumber;

    @NotBlank(message = "Email is required.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format.")
    @Schema(
            description = "Email of the user",
            example = "example@gmail.com"
    )
    private String email;

    private UUID storeId;

    private Set<UUID> roleIds;
}
