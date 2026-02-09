package candi.auth.classic;

import candi.auth.core.CandiUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bridges {@link CandiUser} and Spring Security's {@link UserDetails}.
 * Used internally by the classic auth module to integrate Candi's user model
 * with Spring Security's authentication infrastructure.
 */
public class ClassicCandiUser implements CandiUser, UserDetails {

    private final Object id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final Set<String> roles;
    private final String displayName;
    private final String avatar;

    public ClassicCandiUser(Object id, String username, String email, String passwordHash,
                            Set<String> roles, String displayName, String avatar) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = roles != null ? Set.copyOf(roles) : Set.of();
        this.displayName = displayName;
        this.avatar = avatar;
    }

    /**
     * Creates a ClassicCandiUser from a CandiUser plus a password hash.
     */
    public static ClassicCandiUser from(CandiUser user, String passwordHash) {
        return new ClassicCandiUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                passwordHash,
                user.getRoles(),
                user.getDisplayName(),
                user.getAvatar()
        );
    }

    // CandiUser methods

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

    // UserDetails methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
