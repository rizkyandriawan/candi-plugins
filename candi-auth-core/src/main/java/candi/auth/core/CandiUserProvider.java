package candi.auth.core;

/**
 * Interface for loading user data from the application's persistence layer.
 * Application code must implement this interface and register it as a Spring bean
 * for any authentication module to function.
 */
public interface CandiUserProvider {

    /**
     * Finds a user by username. Returns null if not found.
     */
    CandiUser findByUsername(String username);

    /**
     * Finds a user by ID. Returns null if not found.
     */
    CandiUser findById(Object id);
}
