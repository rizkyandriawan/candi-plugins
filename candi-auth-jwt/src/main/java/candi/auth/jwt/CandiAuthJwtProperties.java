package candi.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT authentication.
 *
 * <p>Example in application.yml:</p>
 * <pre>
 * candi:
 *   auth:
 *     jwt:
 *       secret: my-256-bit-secret-key-for-hmac-sha256
 *       access-token-expiry: 900
 *       refresh-token-expiry: 604800
 *       cookie-name: candi_token
 *       use-cookies: true
 * </pre>
 */
@ConfigurationProperties(prefix = "candi.auth.jwt")
public class CandiAuthJwtProperties {

    /**
     * HMAC-SHA256 signing secret. Must be overridden in production.
     */
    private String secret = "change-me-in-production-please";

    /**
     * Access token expiry in seconds. Default: 900 (15 minutes).
     */
    private long accessTokenExpiry = 900;

    /**
     * Refresh token expiry in seconds. Default: 604800 (7 days).
     */
    private long refreshTokenExpiry = 604800;

    /**
     * Cookie name for storing the JWT token.
     */
    private String cookieName = "candi_token";

    /**
     * If true, JWT is stored in HTTP-only cookies. If false, only
     * the Authorization header is used.
     */
    private boolean useCookies = true;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public void setAccessTokenExpiry(long accessTokenExpiry) {
        this.accessTokenExpiry = accessTokenExpiry;
    }

    public long getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }

    public void setRefreshTokenExpiry(long refreshTokenExpiry) {
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public boolean isUseCookies() {
        return useCookies;
    }

    public void setUseCookies(boolean useCookies) {
        this.useCookies = useCookies;
    }
}
