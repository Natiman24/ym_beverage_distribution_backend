package com.YM.Beverage.Distribution.Backend.configs.security;

import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FilterPermissionGuard {

    private final DataScopeService dataScopeService;

    public void requireWhenFiltering(Permission permission, Object... filters) {
        for (Object filter : filters) {
            if (isPresent(filter)) {
                dataScopeService.currentPermissionScope(permission);
                return;
            }
        }
    }

    private boolean isPresent(Object filter) {
        if (filter == null) return false;
        if (filter instanceof String value) return !value.isBlank();
        if (filter instanceof Collection<?> values) return !values.isEmpty();
        return true;
    }
}
