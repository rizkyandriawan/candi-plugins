package candi.auth.classic;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for classic session-based authentication.
 *
 * <p>Example in application.yml:</p>
 * <pre>
 * candi:
 *   auth:
 *     login-url: /login
 *     session-timeout: 3600
 *     remember-me: true
 *     remember-me-days: 30
 * </pre>
 */
@ConfigurationProperties(prefix = "candi.auth")
public class CandiAuthClassicProperties {

    /**
     * URL to redirect unauthenticated users to.
     */
    private String loginUrl = "/login";

    /**
     * Session timeout in seconds. Default: 3600 (1 hour).
     */
    private int sessionTimeout = 3600;

    /**
     * Whether to enable the remember-me feature.
     */
    private boolean rememberMe = true;

    /**
     * Number of days the remember-me cookie is valid.
     */
    private int rememberMeDays = 30;

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public int getRememberMeDays() {
        return rememberMeDays;
    }

    public void setRememberMeDays(int rememberMeDays) {
        this.rememberMeDays = rememberMeDays;
    }
}
