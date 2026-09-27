package com.YM.Beverage.Distribution.Backend.user.models;


import com.YM.Beverage.Distribution.Backend.utils.global_classes.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "otps")
@Inheritance(strategy = InheritanceType.JOINED)
public class Otp extends BaseEntity {
    @JsonIgnore
    private String code;
    @Column(name = "number_of_tries")
    private Integer numberOfTries;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
