package com.YM.Beverage.Distribution.Backend.configs.security;

import com.YM.Beverage.Distribution.Backend.user.controllers.AuthController;
import com.YM.Beverage.Distribution.Backend.user.models.Permission;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerAuthorizationTest {

    private static final String BASE_PACKAGE = "com.YM.Beverage.Distribution.Backend";
    private static final Set<String> PUBLIC_AUTH_METHODS = Set.of(
            "activateAccount",
            "login",
            "refreshAccessToken",
            "requestOtp",
            "verifyOtp",
            "resetPassword"
    );
    private static final Pattern AUTHORITY_PATTERN = Pattern.compile("hasAuthority\\('([A-Z_]+)'\\)");
    private static final Set<String> KNOWN_PERMISSIONS = Arrays.stream(Permission.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    @Test
    void everyNonPublicEndpointHasAValidAuthorizationRule() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        for (var beanDefinition : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> controller = Class.forName(beanDefinition.getBeanClassName());
            PreAuthorize classRule = AnnotatedElementUtils.findMergedAnnotation(controller, PreAuthorize.class);

            for (Method method : controller.getDeclaredMethods()) {
                if (AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class) == null
                        || isPublicAuthenticationEndpoint(controller, method)) {
                    continue;
                }

                PreAuthorize methodRule = AnnotatedElementUtils.findMergedAnnotation(method, PreAuthorize.class);
                PreAuthorize effectiveRule = methodRule != null ? methodRule : classRule;
                String endpoint = controller.getSimpleName() + "." + method.getName();

                assertTrue(effectiveRule != null, endpoint + " must declare @PreAuthorize");
                assertReferencedAuthorityExists(effectiveRule, endpoint);
            }
        }
    }

    private boolean isPublicAuthenticationEndpoint(Class<?> controller, Method method) {
        return controller == AuthController.class && PUBLIC_AUTH_METHODS.contains(method.getName());
    }

    private void assertReferencedAuthorityExists(PreAuthorize rule, String endpoint) {
        Matcher matcher = AUTHORITY_PATTERN.matcher(rule.value());
        while (matcher.find()) {
            String authority = matcher.group(1);
            assertTrue(KNOWN_PERMISSIONS.contains(authority),
                    endpoint + " references unknown permission " + authority);
        }
    }
}
