package candi.auth.jwt;

import candi.auth.core.CandiUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

/**
 * Service for generating and validating JWT tokens.
 * Uses HMAC-SHA256 signing with a configurable secret key.
 */
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private final SecretKey signingKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtTokenService(CandiAuthJwtProperties properties) {
        // Ensure the secret is at least 32 bytes for HMAC-SHA256
        String secret = properties.getSecret();
        if (secret.length() < 32) {
            secret = String.format("%-32s", secret).replace(' ', '0');
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = properties.getAccessTokenExpiry() * 1000;
        this.refreshTokenExpiryMs = properties.getRefreshTokenExpiry() * 1000;
    }

    /**
     * Generates a short-lived access token for the given user.
     */
    public String generateAccessToken(CandiUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("id", String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("roles", String.join(",", user.getRoles()))
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generates a long-lived refresh token for the given user.
     */
    public String generateRefreshToken(CandiUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiryMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("id", String.valueOf(user.getId()))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates a JWT token. Returns true if the signature is valid and the token
     * has not expired.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts user data from a valid JWT token.
     * Returns null if the token is invalid.
     */
    public JwtUserData getUserFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            String id = claims.get("id", String.class);
            String email = claims.get("email", String.class);
            String rolesStr = claims.get("roles", String.class);

            Set<String> roles = (rolesStr != null && !rolesStr.isEmpty())
                    ? Set.of(rolesStr.split(","))
                    : Set.of();

            return new JwtUserData(id, username, email, roles);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Could not extract user from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Data extracted from a JWT token's claims.
     */
    public record JwtUserData(String id, String username, String email, Set<String> roles) {
    }
}
