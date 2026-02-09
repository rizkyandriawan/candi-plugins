package candi.auth.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Explicitly marks a page as publicly accessible.
 * Overrides any global protection settings, ensuring the page
 * can be accessed without authentication.
 *
 * <p>Example:</p>
 * <pre>
 * {@literal @}Page("/about")
 * {@literal @}Public
 * public class AboutPage implements CandiPage { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Public {
}
