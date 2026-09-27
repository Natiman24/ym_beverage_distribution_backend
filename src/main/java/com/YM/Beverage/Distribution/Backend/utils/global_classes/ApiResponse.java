package com.YM.Beverage.Distribution.Backend.utils.global_classes;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Map;

@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Omit null values
public class ApiResponse {
    private String message;
    private HttpStatus statusCode;

    @JsonInclude(JsonInclude.Include.NON_EMPTY) // Omit empty maps
    private Map<String, Object> details;

    public ApiResponse(String mes, HttpStatus code) {
        this.message = mes;
        this.statusCode = code;
    }

    @JsonAnyGetter
    public Map<String, Object> getDetails() {
        return details;
    }
}
