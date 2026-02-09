package candi.auth.social;

import candi.auth.core.CandiUser;

import java.util.Set;

/**
 * {@link CandiUser} implementation for social login users.
 * Created by {@link DefaultSocialUserMapper} from OAuth2 attributes.
 */
public class SocialCandiUser implements CandiUser {

    private final String id;
    private final String username;
    private final String email;
    private final Set<String> roles;
    private final String displayName;
    private final String avatar;
    private final String provider;

    public SocialCandiUser(String id, String username, String email, Set<String> roles,
                           String displayName, String avatar, String provider) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles != null ? Set.copyOf(roles) : Set.of();
        this.displayName = displayName;
        this.avatar = avatar;
        this.provider = provider;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : username;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    /**
     * Returns the OAuth2 provider that authenticated this user (e.g., "google", "github").
     */
    public String getProvider() {
        return provider;
    }
}
