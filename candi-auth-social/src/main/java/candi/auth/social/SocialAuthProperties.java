package candi.auth.social;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration properties for social OAuth2 authentication.
 *
 * <p>Example in application.yml:</p>
 * <pre>
 * candi:
 *   auth:
 *     social:
 *       success-url: /
 *       failure-url: /login?error
 *       providers:
 *         google:
 *           client-id: xxx.apps.googleusercontent.com
 *           client-secret: xxx
 *         github:
 *           client-id: xxx
 *           client-secret: xxx
 *           scopes:
 *             - read:user
 *             - user:email
 *             - read:org
 * </pre>
 */
@ConfigurationProperties(prefix = "candi.auth.social")
public class SocialAuthProperties {

    /**
     * Map of provider name to provider configuration.
     * Supported providers: google, github, facebook, microsoft, apple.
     */
    private Map<String, ProviderConfig> providers = new LinkedHashMap<>();

    /**
     * URL to redirect to after successful authentication.
     */
    private String successUrl = "/";

    /**
     * URL to redirect to after failed authentication.
     */
    private String failureUrl = "/login?error";

    public Map<String, ProviderConfig> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, ProviderConfig> providers) {
        this.providers = providers;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getFailureUrl() {
        return failureUrl;
    }

    public void setFailureUrl(String failureUrl) {
        this.failureUrl = failureUrl;
    }

    /**
     * Configuration for a single OAuth2 provider.
     */
    public static class ProviderConfig {

        private String clientId;
        private String clientSecret;
        private String[] scopes;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String[] getScopes() {
            return scopes;
        }

        public void setScopes(String[] scopes) {
            this.scopes = scopes;
        }
    }
}
