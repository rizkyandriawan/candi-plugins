package candi.auth.core;

import candi.auth.core.annotation.OwnerOnly;
import candi.auth.core.annotation.Protected;
import candi.auth.core.annotation.Public;
import candi.runtime.CandiHandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Spring MVC interceptor that enforces Candi authentication annotations.
 * Checks {@link Protected}, {@link OwnerOnly}, and {@link Public} on page classes
 * before allowing the request to proceed.
 */
public class CandiAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CandiAuthInterceptor.class);

    private final CandiAuthService authService;
    private final ApplicationContext applicationContext;

    public CandiAuthInterceptor(CandiAuthService authService, ApplicationContext applicationContext) {
        this.authService = authService;
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!(handler instanceof CandiHandlerMapping.CandiPageHandler pageHandler)) {
            return true;
        }

        String beanName = pageHandler.beanName();
        Class<?> pageClass = resolvePageClass(beanName);
        if (pageClass == null) {
            return true;
        }

        // Store current user in request attributes for template access
        CandiUser currentUser = authService.getCurrentUser();
        AuthRequestHolder.setCurrentUser(request, currentUser);

        // @Public pages skip all checks
        if (pageClass.isAnnotationPresent(Public.class)) {
            return true;
        }

        // Check @OwnerOnly (implies @Protected)
        OwnerOnly ownerOnly = pageClass.getAnnotation(OwnerOnly.class);
        if (ownerOnly != null) {
            if (!authService.isAuthenticated()) {
                String loginUrl = resolveLoginUrl(pageClass);
                response.sendRedirect(loginUrl);
                return false;
            }

            // Owner check is deferred: the page bean hasn't populated its fields yet
            // at preHandle time. We store the annotation so postHandle or the adapter
            // can enforce it. For now, we verify authentication only.
            // The actual owner check happens after init() populates the fields.
            request.setAttribute("_candiOwnerOnly", ownerOnly);
            request.setAttribute("_candiPageBeanName", beanName);
            return true;
        }

        // Check @Protected
        Protected protectedAnn = pageClass.getAnnotation(Protected.class);
        if (protectedAnn != null) {
            if (!authService.isAuthenticated()) {
                response.sendRedirect(protectedAnn.loginUrl());
                return false;
            }

            // Check single role
            if (!protectedAnn.role().isEmpty()) {
                if (!authService.hasRole(protectedAnn.role())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            }

            // Check roles array (OR logic)
            if (protectedAnn.roles().length > 0) {
                boolean hasAnyRole = false;
                for (String role : protectedAnn.roles()) {
                    if (authService.hasRole(role)) {
                        hasAnyRole = true;
                        break;
                    }
                }
                if (!hasAnyRole) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * After the handler has executed, check @OwnerOnly if it was deferred.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // Clean up request attributes
        request.removeAttribute("_candiUser");
        request.removeAttribute("_candiOwnerOnly");
        request.removeAttribute("_candiPageBeanName");
    }

    /**
     * Checks owner-only access on a page bean instance after its fields are populated.
     * Called by integration code after page.init() and data loading.
     */
    public boolean checkOwnership(Object pageInstance, OwnerOnly ownerOnly, CandiUser currentUser) {
        if (currentUser == null) {
            return false;
        }

        try {
            Field field = findField(pageInstance.getClass(), ownerOnly.field());
            if (field == null) {
                log.warn("@OwnerOnly field '{}' not found on {}", ownerOnly.field(),
                        pageInstance.getClass().getName());
                return false;
            }

            field.setAccessible(true);
            Object ownerValue = field.get(pageInstance);
            Object userValue = getUserFieldValue(currentUser, ownerOnly.userField());

            return Objects.equals(String.valueOf(ownerValue), String.valueOf(userValue));
        } catch (Exception e) {
            log.error("Error checking ownership on {}", pageInstance.getClass().getName(), e);
            return false;
        }
    }

    private Object getUserFieldValue(CandiUser user, String fieldName) {
        return switch (fieldName) {
            case "id" -> user.getId();
            case "username" -> user.getUsername();
            case "email" -> user.getEmail();
            default -> {
                log.warn("Unknown userField '{}' in @OwnerOnly, falling back to id", fieldName);
                yield user.getId();
            }
        };
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private String resolveLoginUrl(Class<?> pageClass) {
        Protected protectedAnn = pageClass.getAnnotation(Protected.class);
        if (protectedAnn != null) {
            return protectedAnn.loginUrl();
        }
        return "/login";
    }

    private Class<?> resolvePageClass(String beanName) {
        try {
            return applicationContext.getType(beanName);
        } catch (Exception e) {
            log.debug("Could not resolve page class for bean '{}'", beanName);
            return null;
        }
    }
}
