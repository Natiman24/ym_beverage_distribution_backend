package com.YM.Beverage.Distribution.Backend.configs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of(
            "/api/auth/activate",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/request-otp",
            "/api/auth/verify-otp",
            "/api/auth/reset-password"
    );

    private final JwtUtil jwtUtil;
    private final ObjectMapper mapper;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        return PUBLIC_AUTH_PATHS.contains(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {

        try {
            String accessToken = jwtUtil.resolveToken(request);

            if (accessToken == null) {
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims = jwtUtil.resolveClaims(request);

            if (claims == null || !jwtUtil.validateClaims(claims)) {
                writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token", "Token validation failed");
                return;
            }


            String email = jwtUtil.getEmail(claims);

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (IllegalArgumentException e) {
            writeErrorResponse(response, HttpStatus.FORBIDDEN, "Invalid token", e.getMessage());
        } catch (ExpiredJwtException e) {
            writeErrorResponse(response, HttpStatus.FORBIDDEN, "Token has expired, Please login again",
                    e.getMessage());
        } catch (Exception e) {
            writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Some error has occurred", e.getMessage());
        }
        // Note: TenantContext cleanup is now handled by TenantContextCleanupFilter
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message, String details)
            throws IOException {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("message", message);
        errorDetails.put("exception", details);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), errorDetails);
    }

}
