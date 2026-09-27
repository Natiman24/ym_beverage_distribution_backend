package com.YM.Beverage.Distribution.Backend.configs.security;

import com.YM.Beverage.Distribution.Backend.user.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    @Value("${secret_key}")
    private String secretKey;

    @Value("${access_token_validity}")
    private long accessTokenValidity;

    @Value("${refresh_token_validity}")
    private long refreshTokenValidity;

    private JwtParser jwtParser;
    private SecretKey signingKey;

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @PostConstruct
    public void init() {

        signingKey = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );

        jwtParser = Jwts.parser()
                .verifyWith(signingKey)
                .build();
    }

    public String createAccessToken(User user) {

        Date issuedAt = new Date();

        Date expiration = new Date(
                issuedAt.getTime()
                        + TimeUnit.MINUTES.toMillis(accessTokenValidity)
        );

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public String createRefreshToken(UUID userId) {

        Date issuedAt = new Date();

        Date expiration = new Date(
                issuedAt.getTime()
                        + TimeUnit.DAYS.toMillis(refreshTokenValidity)
        );

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    private Claims parseJwtClaims(String token) {

        return jwtParser
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims resolveClaims(HttpServletRequest request) {

        try {

            String token = resolveToken(request);

            if (token != null) {
                return parseJwtClaims(token);
            }

            return null;

        } catch (ExpiredJwtException ex) {

            request.setAttribute("expired", ex.getMessage());
            throw ex;

        } catch (Exception ex) {

            request.setAttribute("invalid", ex.getMessage());
            throw ex;
        }
    }

    public String resolveToken(HttpServletRequest request) {

        try {

            String bearerToken = request.getHeader(TOKEN_HEADER);

            if (bearerToken != null
                    && bearerToken.startsWith(TOKEN_PREFIX)) {

                return bearerToken.substring(TOKEN_PREFIX.length());
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {

        try {

            Claims claims = parseJwtClaims(token);

            return claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateClaims(Claims claims)
            throws AuthenticationException {

        try {

            return claims.getExpiration().after(new Date());

        } catch (Exception e) {
            throw e;
        }
    }

    public UUID getUserId(String token) {

        if (token == null) {
            return null;
        }

        Claims claims = parseJwtClaims(token);

        return UUID.fromString(
                claims.getSubject()
        );
    }

    public String getEmail(Claims claims) {

        return claims.get("email", String.class);
    }

    public String getName(String token) {

        Claims claims = parseJwtClaims(token);

        String firstName =
                claims.get("firstName", String.class);

        String lastName =
                claims.get("lastName", String.class);

        return firstName + " " + lastName;
    }

    public static String getCurrentUserEmail() {

        var context =
                SecurityContextHolder.getContext();

        var authentication =
                context.getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(
                authentication.getPrincipal()
        )) {

            return authentication.getName();
        }

        return null;
    }

    public UUID getUserIdFromRequest(
            HttpServletRequest request
    ) {

        return getUserId(
                resolveToken(request)
        );
    }
}
