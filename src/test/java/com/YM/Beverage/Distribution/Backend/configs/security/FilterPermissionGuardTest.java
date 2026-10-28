package com.YM.Beverage.Distribution.Backend.configs.security;

import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FilterPermissionGuardTest {

    @Mock private DataScopeService dataScopeService;
    @InjectMocks private FilterPermissionGuard guard;

    @Test
    void doesNotRequireFilterPermissionForAbsentFilters() {
        guard.requireWhenFiltering(Permission.ORDER_FILTER, null, "  ", List.of());

        verify(dataScopeService, never()).currentPermissionScope(Permission.ORDER_FILTER);
    }

    @Test
    void requiresFilterPermissionForAValue() {
        guard.requireWhenFiltering(Permission.ORDER_FILTER, null, "submitted");

        verify(dataScopeService).currentPermissionScope(Permission.ORDER_FILTER);
    }

    @Test
    void treatsFalseAsAnExplicitBooleanFilter() {
        guard.requireWhenFiltering(Permission.PRODUCT_FILTER, false);

        verify(dataScopeService).currentPermissionScope(Permission.PRODUCT_FILTER);
    }
}
