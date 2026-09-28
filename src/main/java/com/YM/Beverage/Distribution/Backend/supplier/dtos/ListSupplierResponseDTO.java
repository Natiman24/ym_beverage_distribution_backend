package com.YM.Beverage.Distribution.Backend.supplier.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListSupplierResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private String phoneNumber;
    private Boolean active;
}
