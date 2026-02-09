package candi.auth.social;

import candi.auth.core.CandiUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link SocialUserMapper} that maps standard OAuth2
 * attributes to {@link SocialCandiUser} for each supported provider.
 *
 * <p>Provider-specific attribute mapping:</p>
 * <ul>
 *   <li>Google: email, name, picture, sub</li>
 *   <li>GitHub: login, email, avatar_url, id</li>
 *   <li>Facebook: email, name, id</li>
 *   <li>Microsoft: email, displayName, id</li>
 *   <li>Apple: email, sub</li>
 * </ul>
 */
public class DefaultSocialUserMapper implements SocialUserMapper {

    @Override
    public CandiUser mapUser(String provider, OAuth2User oAuth2User) {
        Map<String, Object> attrs = oAuth2User.getAttributes();

        return switch (provider.toLowerCase()) {
            case "google" -> mapGoogle(attrs);
            case "github" -> mapGitHub(attrs);
            case "facebook" -> mapFacebook(attrs);
            case "microsoft" -> mapMicrosoft(attrs);
            case "apple" -> mapApple(attrs);
            default -> mapGeneric(provider, attrs);
        };
    }

    private SocialCandiUser mapGoogle(Map<String, Object> attrs) {
        return new SocialCandiUser(
                getString(attrs, "sub"),
                getString(attrs, "email"),
                getString(attrs, "email"),
                Set.of("user"),
                getString(attrs, "name"),
                getString(attrs, "picture"),
                "google"
        );
    }

    private SocialCandiUser mapGitHub(Map<String, Object> attrs) {
        String id = attrs.containsKey("id") ? String.valueOf(attrs.get("id")) : null;
        String login = getString(attrs, "login");

        return new SocialCandiUser(
                id,
                login,
                getString(attrs, "email"),
                Set.of("user"),
                getString(attrs, "name"),
                getString(attrs, "avatar_url"),
                "github"
        );
    }

    private SocialCandiUser mapFacebook(Map<String, Object> attrs) {
        return new SocialCandiUser(
                getString(attrs, "id"),
                getString(attrs, "email"),
                getString(attrs, "email"),
                Set.of("user"),
                getString(attrs, "name"),
                null,
                "facebook"
        );
    }

    private SocialCandiUser mapMicrosoft(Map<String, Object> attrs) {
        return new SocialCandiUser(
                getString(attrs, "id"),
                getString(attrs, "email"),
                getString(attrs, "email"),
                Set.of("user"),
                getString(attrs, "displayName"),
                null,
                "microsoft"
        );
    }

    private SocialCandiUser mapApple(Map<String, Object> attrs) {
        String sub = getString(attrs, "sub");
        String email = getString(attrs, "email");

        return new SocialCandiUser(
                sub,
                email != null ? email : sub,
                email,
                Set.of("user"),
                null,
                null,
                "apple"
        );
    }

    private SocialCandiUser mapGeneric(String provider, Map<String, Object> attrs) {
        String id = getString(attrs, "id");
        if (id == null) {
            id = getString(attrs, "sub");
        }
        String email = getString(attrs, "email");
        String name = getString(attrs, "name");
        if (name == null) {
            name = getString(attrs, "displayName");
        }

        return new SocialCandiUser(
                id,
                email != null ? email : id,
                email,
                Set.of("user"),
                name,
                getString(attrs, "picture"),
                provider
        );
    }

    private String getString(Map<String, Object> attrs, String key) {
        Object value = attrs.get(key);
        return value != null ? value.toString() : null;
    }
}
