package com.YM.Beverage.Distribution.Backend.user.models;

import com.YM.Beverage.Distribution.Backend.user.dtos.role.RoleListResponseDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.role.RoleResponseDTO;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
@Inheritance(strategy = InheritanceType.JOINED)
public class Role extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<Permission> permissions = new HashSet<>();

    public RoleResponseDTO toResponseDTO() {
        return RoleResponseDTO.builder()
                .id(getId())
                .name(getName())
                .permissions(getPermissions())
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }

    public RoleListResponseDTO toListResponseDTO() {
        return RoleListResponseDTO.builder()
                .id(getId())
                .name(getName())
                .build();
    }
}
