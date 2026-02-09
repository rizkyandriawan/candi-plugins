package candi.auth.core;

import java.util.Set;

/**
 * Core user interface for Candi authentication.
 * All authentication modules produce and consume {@code CandiUser} instances.
 * Application code should implement this interface for their user domain model.
 */
public interface CandiUser {

    /**
     * Returns the unique identifier for this user.
     */
    Object getId();

    /**
     * Returns the username used for authentication.
     */
    String getUsername();

    /**
     * Returns the user's email address.
     */
    String getEmail();

    /**
     * Returns the set of roles assigned to this user.
     */
    Set<String> getRoles();

    /**
     * Returns a human-readable display name. Defaults to username.
     */
    default String getDisplayName() {
        return getUsername();
    }

    /**
     * Returns the URL to the user's avatar image, or null if none.
     */
    default String getAvatar() {
        return null;
    }
}
