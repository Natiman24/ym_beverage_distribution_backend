package com.YM.Beverage.Distribution.Backend.store.dtos;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListStoreResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private String tinNumber;
    private String phoneNumber;
    private String email;
    private String city;
    private String subCity;
    private String address;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
