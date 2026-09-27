package com.YM.Beverage.Distribution.Backend.user.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {
    @Schema(
            description = "Email of the user",
            example = "example@gmail.com"
    )
    private String email;

    @Schema(
            description = "Password of the user",
            example = "Abc@1234"
    )
    private String password;
}
