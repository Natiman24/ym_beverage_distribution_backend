package com.YM.Beverage.Distribution.Backend.utils.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DataNotFoundException extends RuntimeException{

    private String message;
}
