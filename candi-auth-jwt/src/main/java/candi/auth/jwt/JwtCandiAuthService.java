package candi.auth.jwt;

import candi.auth.core.CandiAuthService;
import candi.auth.core.CandiUser;
import candi.auth.core.CandiUserProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * JWT-based implementation of {@link CandiAuthService}.
 * On login, generates an access token and optional refresh token.
 * Tokens are stored in HTTP-only cookies when configured, or returned
 * for the caller to handle (e.g., in an API response body).
 */
public class JwtCandiAuthService implements CandiAuthService {

    private final JwtTokenService tokenService;
    private final CandiUserProvider userProvider;
    private final PasswordEncoder passwordEncoder;
    private final CandiAuthJwtProperties properties;

    public JwtCandiAuthService(JwtTokenService tokenService, CandiUserProvider userProvider,
                               PasswordEncoder passwordEncoder, CandiAuthJwtProperties properties) {
        this.tokenService = tokenService;
        this.userProvider = userProvider;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    public CandiUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CandiUser candiUser) {
            return candiUser;
        }

        return null;
    }

    @Override
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getPrincipal() instanceof CandiUser;
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
        CandiUser user = userProvider.findByUsername(username);
        if (user == null) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid username or password");
        }

        // Verify password
        String storedHash = extractPasswordHash(user);
        if (!passwordEncoder.matches(password, storedHash)) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid username or password");
        }

        // Generate tokens
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        // Set authentication in security context
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Store tokens in cookies if configured
        if (properties.isUseCookies()) {
            HttpServletResponse response = getCurrentResponse();
            if (response != null) {
                addTokenCookie(response, properties.getCookieName(), accessToken,
                        (int) properties.getAccessTokenExpiry());
                addTokenCookie(response, properties.getCookieName() + "_refresh", refreshToken,
                        (int) properties.getRefreshTokenExpiry());
            }
        }
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();

        if (properties.isUseCookies()) {
            HttpServletResponse response = getCurrentResponse();
            if (response != null) {
                clearCookie(response, properties.getCookieName());
                clearCookie(response, properties.getCookieName() + "_refresh");
            }
        }
    }

    /**
     * Returns the most recently generated access token, useful for API responses.
     */
    public String getAccessToken(CandiUser user) {
        return tokenService.generateAccessToken(user);
    }

    /**
     * Returns the most recently generated refresh token.
     */
    public String getRefreshToken(CandiUser user) {
        return tokenService.generateRefreshToken(user);
    }

    private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private HttpServletResponse getCurrentResponse() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getResponse();
        }
        return null;
    }

    private String extractPasswordHash(CandiUser user) {
        if (user instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getPassword();
        }
        try {
            var method = user.getClass().getMethod("getPassword");
            Object result = method.invoke(user);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
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
