package candi.auth.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code @Page} class as requiring authentication.
 * When placed on a page, the {@link candi.auth.core.CandiAuthInterceptor} will
 * verify that the current request is authenticated before allowing access.
 *
 * <p>Optionally restricts access to specific roles using {@link #role()} or {@link #roles()}.
 * If multiple roles are specified, OR logic is applied (any matching role grants access).</p>
 *
 * <p>Example usage in a .jhtml file:</p>
 * <pre>
 * {@literal @}Page("/dashboard")
 * {@literal @}Protected(role = "admin")
 * public class DashboardPage implements CandiPage { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Protected {

    /**
     * Required role. Default is empty string, meaning any authenticated user is allowed.
     */
    String role() default "";

    /**
     * Array of allowed roles (OR logic). If non-empty, the user must have at least one.
     */
    String[] roles() default {};

    /**
     * URL to redirect to if the user is not authenticated.
     */
    String loginUrl() default "/login";
}
