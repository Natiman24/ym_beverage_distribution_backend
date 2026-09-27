package com.YM.Beverage.Distribution.Backend.driver.dtos;

import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDriverDTO {
    private String firstName;
    private String lastName;

    @Pattern(regexp = "^\\+251[0-9]{9}$", message = "Phone number must start with +251 and contain exactly 9 digits after that.")
    private String phoneNumber;

    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format.")
    private String email;

    private String licenseNumber;
}
