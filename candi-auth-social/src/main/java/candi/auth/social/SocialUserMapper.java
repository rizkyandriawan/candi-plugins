package candi.auth.social;

import candi.auth.core.CandiUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Maps an OAuth2 user from a social provider to the application's {@link CandiUser}.
 *
 * <p>Applications can provide a custom implementation to control how OAuth2 attributes
 * are mapped to their user model. For example, auto-creating users on first login,
 * linking social accounts to existing users, or extracting custom attributes.</p>
 *
 * <p>If no custom mapper is provided, {@link DefaultSocialUserMapper} is used.</p>
 */
public interface SocialUserMapper {

    /**
     * Maps an OAuth2 user to a CandiUser.
     *
     * @param provider the provider name (e.g., "google", "github")
     * @param oAuth2User the OAuth2 user from the provider
     * @return a CandiUser representing the authenticated user
     */
    CandiUser mapUser(String provider, OAuth2User oAuth2User);
}
