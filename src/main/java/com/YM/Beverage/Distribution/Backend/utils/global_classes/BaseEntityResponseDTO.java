package com.YM.Beverage.Distribution.Backend.utils.global_classes;

import com.YM.Beverage.Distribution.Backend.user.dtos.profile.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseEntityResponseDTO {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserResponseDTO createdBy;
    private UserResponseDTO updatedBy;
}
