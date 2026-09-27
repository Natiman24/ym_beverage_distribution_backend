package com.YM.Beverage.Distribution.Backend.user.models;

import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.UserListResponseDTO;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.UserResponseDTO;
import com.YM.Beverage.Distribution.Backend.utils.global_classes.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User extends BaseEntity {
    @Column(name = "first_name" , nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @JsonIgnore
    private String password;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "is_deactivated", nullable = false)
    private boolean isDeactivated;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<Permission> permissions = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id")
    private Store store;


    public UserResponseDTO toUserResponseDTO(){
        return UserResponseDTO.builder()
                .id(getId())
                .firstName(getFirstName())
                .lastName(getLastName())
                .phoneNumber(getPhoneNumber())
                .email(getEmail())
                .isActive(isActive)
                .isDeactivated(isDeactivated)
                .roles(getRoles().stream().map(Role::toResponseDTO).collect(java.util.stream.Collectors.toSet()))
                .permissions(getPermissions())
                .storeId(store != null ? store.getId() : null)
                .storeName(store != null ? store.getName() : null)
                .build();
    }

    public UserListResponseDTO toUserListResponseDTO() {
        return UserListResponseDTO.builder()
                .id(getId())
                .firstName(getFirstName())
                .lastName(getLastName())
                .phoneNumber(getPhoneNumber())
                .email(getEmail())
                .isActive(isActive)
                .isDeactivated(isDeactivated())
                .roles(getRoles().stream().map(Role::toListResponseDTO).collect(java.util.stream.Collectors.toSet()))
                .storeId(store != null ? store.getId() : null)
                .storeName(store != null ? store.getName() : null)
                .build();
    }
}
