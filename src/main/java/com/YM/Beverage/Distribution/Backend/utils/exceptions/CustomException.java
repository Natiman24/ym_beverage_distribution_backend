package com.YM.Beverage.Distribution.Backend.utils.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomException extends RuntimeException{

    private String message;
    private HttpStatusCode statusCode;
    private String exception;
}
