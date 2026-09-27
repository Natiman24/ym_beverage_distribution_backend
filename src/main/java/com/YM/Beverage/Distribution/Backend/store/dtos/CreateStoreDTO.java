package com.YM.Beverage.Distribution.Backend.store.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStoreDTO {
    @NotNull
    @NotBlank
    private String name;
    private String description;
    private String tinNumber;
    private String phoneNumber;
    private String email;
    private String city;
    private String subCity;
    private String address;
}
