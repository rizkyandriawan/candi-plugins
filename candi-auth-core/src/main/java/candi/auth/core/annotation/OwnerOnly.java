package candi.auth.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code @Page} as accessible only by the resource owner.
 * The interceptor compares the value of the specified field on the page
 * to the current user's identifier.
 *
 * <p>Implies {@code @Protected} behavior: unauthenticated users are redirected to login.</p>
 *
 * <p>Example:</p>
 * <pre>
 * {@literal @}Page("/profile/{userId}")
 * {@literal @}OwnerOnly(field = "userId")
 * public class ProfileEditPage implements CandiPage {
 *     private String userId;
 *     ...
 * }
 * </pre>
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OwnerOnly {

    /**
     * Field name on the page class that contains the owner's ID or username.
     */
    String field();

    /**
     * Which {@link candi.auth.core.CandiUser} field to compare against.
     * Supported values: "id", "username", "email".
     * Default is "id".
     */
    String userField() default "id";
}
