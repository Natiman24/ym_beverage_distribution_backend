package com.YM.Beverage.Distribution.Backend.supplier.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonMerge;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateSupplierDTO{
    @JsonMerge
    private String name;
    @JsonMerge
    private String description;
    @JsonMerge
    private String phoneNumber;
}
