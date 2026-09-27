package com.YM.Beverage.Distribution.Backend.product.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Shared update request for ProductCategory, ProductUnit and VolumeUnit.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateLookupDTO {
    private String name;
    private String description;
    private Boolean active;
}
