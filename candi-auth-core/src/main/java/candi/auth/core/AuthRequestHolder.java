package candi.auth.core;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Stores the current authenticated user in request attributes so that
 * templates and page classes can access it without injecting {@link CandiAuthService}.
 *
 * <p>The user is stored as the {@code _candiUser} request attribute by
 * {@link CandiAuthInterceptor} at the start of each request.</p>
 */
public final class AuthRequestHolder {

    /** Request attribute key for the current user. */
    public static final String USER_ATTRIBUTE = "_candiUser";

    private AuthRequestHolder() {
        // utility class
    }

    /**
     * Sets the current user on the given request.
     */
    public static void setCurrentUser(HttpServletRequest request, CandiUser user) {
        if (user != null) {
            request.setAttribute(USER_ATTRIBUTE, user);
        }
    }

    /**
     * Returns the current user from the active request context, or null if
     * not authenticated or not in a request scope.
     */
    public static CandiUser getCurrentUser() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            Object user = servletAttrs.getRequest().getAttribute(USER_ATTRIBUTE);
            if (user instanceof CandiUser candiUser) {
                return candiUser;
            }
        }
        return null;
    }
}
