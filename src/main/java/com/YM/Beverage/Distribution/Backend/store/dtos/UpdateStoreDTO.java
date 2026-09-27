package com.YM.Beverage.Distribution.Backend.store.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateStoreDTO {
    private String name;
    private String description;
    private String tinNumber;
    private String phoneNumber;
    private String email;
    private String city;
    private String subCity;
    private String address;
}
