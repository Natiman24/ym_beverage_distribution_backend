package com.YM.Beverage.Distribution.Backend.utils.global_classes;

import com.YM.Beverage.Distribution.Backend.configs.security.JwtUtil;
import com.YM.Beverage.Distribution.Backend.user.models.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<User> {
    private final JwtUtil jwtUtil;
    private final HttpServletRequest httpServletRequest;
    private static final ThreadLocal<Boolean> isRunning = ThreadLocal.withInitial(() -> false);

    @Override
    public Optional<User> getCurrentAuditor() {
        if (isRunning.get()) {
            return Optional.empty(); // prevent recursion
        }

        try {
            isRunning.set(true);
            UUID userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
            if (userId == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(User.builder()
                    .id(userId)
                    .build());
        } finally {
            isRunning.set(false);
        }
    }
}


