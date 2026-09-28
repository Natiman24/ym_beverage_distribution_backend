package com.YM.Beverage.Distribution.Backend.user.services;

import com.YM.Beverage.Distribution.Backend.configs.security.DataScopeService;
import com.YM.Beverage.Distribution.Backend.store.models.Store;
import com.YM.Beverage.Distribution.Backend.store.repositories.StoreRepository;
import com.YM.Beverage.Distribution.Backend.user.dtos.profile.UpdateUserStoreDTO;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import com.YM.Beverage.Distribution.Backend.user.repositories.RoleRepository;
import com.YM.Beverage.Distribution.Backend.user.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private DataScopeService dataScopeService;

    @Test
    void administratorCanAssignAndRemoveUserStore() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("Store")
                .email("store.user@example.com")
                .roles(new HashSet<>())
                .permissions(new HashSet<>())
                .build();
        Store store = Store.builder().id(UUID.randomUUID()).name("Hotel").active(true).build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(storeRepository.findByIdAndActiveTrue(store.getId())).thenReturn(Optional.of(store));

        UserService service = new UserService(userRepository, roleRepository, storeRepository, dataScopeService);
        service.updateUserStore(user.getId(), new UpdateUserStoreDTO(store.getId()));
        assertEquals(store, user.getStore());

        service.updateUserStore(user.getId(), new UpdateUserStoreDTO(null));
        assertNull(user.getStore());
    }
}
