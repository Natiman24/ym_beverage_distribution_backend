package com.YM.Beverage.Distribution.Backend.user.dtos.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditUserResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}
