package candi.auth.social;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import java.util.Map;
import java.util.Set;

/**
 * Default OAuth2 endpoint configurations for each supported social provider.
 * Provides pre-configured {@link ClientRegistration.Builder} instances with
 * standard authorization, token, and user-info URIs.
 */
public final class SocialProviderDefaults {

    private SocialProviderDefaults() {
        // utility class
    }

    /** Default scopes for each provider. */
    private static final Map<String, Set<String>> DEFAULT_SCOPES = Map.of(
            "google", Set.of("openid", "email", "profile"),
            "github", Set.of("read:user", "user:email"),
            "facebook", Set.of("email", "public_profile"),
            "microsoft", Set.of("openid", "email", "profile"),
            "apple", Set.of("openid", "email", "name")
    );

    /**
     * Returns the default scopes for a provider, or a minimal set if unknown.
     */
    public static Set<String> getDefaultScopes(String provider) {
        return DEFAULT_SCOPES.getOrDefault(provider.toLowerCase(), Set.of("openid", "email"));
    }

    /**
     * Creates a pre-configured ClientRegistration builder for the given provider.
     */
    public static ClientRegistration.Builder forProvider(String provider, String clientId,
                                                          String clientSecret, String[] scopeOverrides) {
        String[] scopes = scopeOverrides != null && scopeOverrides.length > 0
                ? scopeOverrides
                : getDefaultScopes(provider).toArray(new String[0]);

        return switch (provider.toLowerCase()) {
            case "google" -> google(clientId, clientSecret, scopes);
            case "github" -> github(clientId, clientSecret, scopes);
            case "facebook" -> facebook(clientId, clientSecret, scopes);
            case "microsoft" -> microsoft(clientId, clientSecret, scopes);
            case "apple" -> apple(clientId, clientSecret, scopes);
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
        };
    }

    private static ClientRegistration.Builder google(String clientId, String clientSecret, String[] scopes) {
        return ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope(scopes)
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google");
    }

    private static ClientRegistration.Builder github(String clientId, String clientSecret, String[] scopes) {
        return ClientRegistration.withRegistrationId("github")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope(scopes)
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .clientName("GitHub");
    }

    private static ClientRegistration.Builder facebook(String clientId, String clientSecret, String[] scopes) {
        return ClientRegistration.withRegistrationId("facebook")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope(scopes)
                .authorizationUri("https://www.facebook.com/v18.0/dialog/oauth")
                .tokenUri("https://graph.facebook.com/v18.0/oauth/access_token")
                .userInfoUri("https://graph.facebook.com/me?fields=id,name,email")
                .userNameAttributeName("id")
                .clientName("Facebook");
    }

    private static ClientRegistration.Builder microsoft(String clientId, String clientSecret, String[] scopes) {
        return ClientRegistration.withRegistrationId("microsoft")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope(scopes)
                .authorizationUri("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
                .tokenUri("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                .userInfoUri("https://graph.microsoft.com/oidc/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://login.microsoftonline.com/common/discovery/v2.0/keys")
                .clientName("Microsoft");
    }

    private static ClientRegistration.Builder apple(String clientId, String clientSecret, String[] scopes) {
        return ClientRegistration.withRegistrationId("apple")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope(scopes)
                .authorizationUri("https://appleid.apple.com/auth/authorize")
                .tokenUri("https://appleid.apple.com/auth/token")
                .userInfoUri("https://appleid.apple.com/auth/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri("https://appleid.apple.com/auth/keys")
                .clientName("Apple");
    }
}
