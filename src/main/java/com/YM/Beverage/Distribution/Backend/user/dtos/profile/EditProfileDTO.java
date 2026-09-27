package com.YM.Beverage.Distribution.Backend.user.dtos.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonMerge;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EditProfileDTO {

    @JsonMerge
    private String firstName;

    @JsonMerge
    private String lastName;

    @JsonMerge
    private String phoneNumber;
}
