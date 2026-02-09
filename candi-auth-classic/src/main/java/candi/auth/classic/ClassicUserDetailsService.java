package candi.auth.classic;

import candi.auth.core.CandiUser;
import candi.auth.core.CandiUserProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Spring Security {@link UserDetailsService} that delegates user loading
 * to the application's {@link CandiUserProvider}.
 * Wraps the returned {@link CandiUser} into a {@link ClassicCandiUser}
 * that implements both interfaces.
 */
public class ClassicUserDetailsService implements UserDetailsService {

    private final CandiUserProvider userProvider;

    public ClassicUserDetailsService(CandiUserProvider userProvider) {
        this.userProvider = userProvider;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CandiUser user = userProvider.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // If the provider already returns a ClassicCandiUser, use it directly
        if (user instanceof ClassicCandiUser classic) {
            return classic;
        }

        // Wrap the CandiUser — the password must be available via the provider's implementation
        // For security, we assume the CandiUser implementation also stores the password hash
        String passwordHash = extractPasswordHash(user);
        return ClassicCandiUser.from(user, passwordHash);
    }

    /**
     * Attempts to extract a password hash from the CandiUser.
     * If the user implements UserDetails, uses getPassword().
     * Otherwise, applications should provide ClassicCandiUser instances or
     * implement UserDetails on their CandiUser.
     */
    private String extractPasswordHash(CandiUser user) {
        if (user instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getPassword();
        }
        // Fallback: try reflection for a getPassword or getPasswordHash method
        try {
            var method = user.getClass().getMethod("getPassword");
            Object result = method.invoke(user);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            // Try getPasswordHash
            try {
                var method = user.getClass().getMethod("getPasswordHash");
                Object result = method.invoke(user);
                return result != null ? result.toString() : "";
            } catch (Exception e2) {
                return "";
            }
        }
    }
}
