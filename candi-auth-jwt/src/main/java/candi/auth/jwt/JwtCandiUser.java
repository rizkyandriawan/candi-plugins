package candi.auth.jwt;

import candi.auth.core.CandiUser;

import java.util.Set;

/**
 * {@link CandiUser} implementation backed by JWT claims data.
 * Created by the {@link JwtAuthenticationFilter} when validating incoming tokens.
 */
public class JwtCandiUser implements CandiUser {

    private final String id;
    private final String username;
    private final String email;
    private final Set<String> roles;

    public JwtCandiUser(String id, String username, String email, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles != null ? Set.copyOf(roles) : Set.of();
    }

    public static JwtCandiUser from(JwtTokenService.JwtUserData data) {
        return new JwtCandiUser(data.id(), data.username(), data.email(), data.roles());
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
}
