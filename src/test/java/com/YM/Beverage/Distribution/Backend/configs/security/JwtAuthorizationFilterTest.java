package com.YM.Beverage.Distribution.Backend.configs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class JwtAuthorizationFilterTest {

    private final JwtAuthorizationFilter filter = new JwtAuthorizationFilter(
            mock(JwtUtil.class), new ObjectMapper(), mock(CustomUserDetailsService.class));

    @Test
    void staleAuthorizationHeaderCannotBlockPublicAuthenticationEndpoints() {
        assertTrue(filter.shouldNotFilter(request("POST", "/api/auth/login")));
        assertTrue(filter.shouldNotFilter(request("POST", "/api/auth/refresh")));
        assertTrue(filter.shouldNotFilter(request("POST", "/api/auth/activate")));
        assertTrue(filter.shouldNotFilter(request("POST", "/api/auth/request-otp")));
        assertTrue(filter.shouldNotFilter(request("POST", "/api/auth/verify-otp")));
        assertTrue(filter.shouldNotFilter(request("POST", "/api/auth/reset-password")));
    }

    @Test
    void logoutAndProtectedEndpointsStillUseJwtValidation() {
        assertFalse(filter.shouldNotFilter(request("POST", "/api/auth/logout")));
        assertFalse(filter.shouldNotFilter(request("POST", "/api/auth/register")));
        assertFalse(filter.shouldNotFilter(request("GET", "/api/orders")));
        assertTrue(filter.shouldNotFilter(request("OPTIONS", "/api/orders")));
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRequestURI(path);
        return request;
    }
}
