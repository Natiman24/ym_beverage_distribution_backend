package com.YM.Beverage.Distribution.Backend.product.dtos;

import lombok.*;

import java.util.UUID;

/**
 * Shared lightweight response for ProductCategory, ProductUnit and VolumeUnit.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LookupResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private Boolean active;
}
