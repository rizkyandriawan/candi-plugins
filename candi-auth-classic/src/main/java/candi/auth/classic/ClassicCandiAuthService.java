package candi.auth.classic;

import candi.auth.core.CandiAuthService;
import candi.auth.core.CandiUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Session-based implementation of {@link CandiAuthService}.
 * Uses Spring Security's {@link SecurityContextHolder} to manage authentication state,
 * backed by HTTP sessions for persistence across requests.
 */
public class ClassicCandiAuthService implements CandiAuthService {

    private final AuthenticationManager authenticationManager;

    public ClassicCandiAuthService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public CandiUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof ClassicCandiUser candiUser) {
            return candiUser;
        }

        // Not a Candi user (e.g., anonymousUser string)
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        // Spring Security sets an anonymous authentication by default
        return auth.getPrincipal() instanceof ClassicCandiUser;
    }

    @Override
    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String roleAuthority = "ROLE_" + role;
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (roleAuthority.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void login(String username, String password) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authenticationManager.authenticate(token);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Persist to session
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        }
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();

        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
    }

    private HttpServletRequest getCurrentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }
}
