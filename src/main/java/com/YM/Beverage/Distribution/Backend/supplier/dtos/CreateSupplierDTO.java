package com.YM.Beverage.Distribution.Backend.supplier.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSupplierDTO {
    private String name;
    private String description;
    private String phoneNumber;
}
