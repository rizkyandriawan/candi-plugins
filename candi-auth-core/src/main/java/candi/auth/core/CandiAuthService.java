package candi.auth.core;

import org.springframework.security.core.AuthenticationException;

/**
 * Core authentication service interface for Candi.
 * Each authentication strategy (classic, JWT, social) provides its own implementation.
 * Page classes can {@code @Autowired} this interface to perform auth operations.
 */
public interface CandiAuthService {

    /**
     * Returns the currently authenticated user, or null if not authenticated.
     */
    CandiUser getCurrentUser();

    /**
     * Returns true if the current request is authenticated.
     */
    boolean isAuthenticated();

    /**
     * Returns true if the current user has the specified role.
     */
    boolean hasRole(String role);

    /**
     * Authenticates with username and password.
     *
     * @param username the username
     * @param password the raw password
     * @throws AuthenticationException if authentication fails
     */
    void login(String username, String password) throws AuthenticationException;

    /**
     * Logs out the current user, clearing any session or token state.
     */
    void logout();
}
