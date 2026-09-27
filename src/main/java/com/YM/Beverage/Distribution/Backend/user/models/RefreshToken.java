package com.YM.Beverage.Distribution.Backend.user.models;

import com.YM.Beverage.Distribution.Backend.utils.global_classes.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "refresh_tokens")
@Inheritance(strategy = InheritanceType.JOINED)
public class RefreshToken extends BaseEntity {
    private String token;
    @Column(name = "expiry_date")
    private Long expiryDate;
    @Column(name = "user_id")
    private UUID userId;
}
