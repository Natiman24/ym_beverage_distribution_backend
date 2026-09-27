package com.YM.Beverage.Distribution.Backend.product.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Shared create request for ProductCategory, ProductUnit and VolumeUnit.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLookupDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
