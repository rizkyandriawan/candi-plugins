package candi.web.seo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares SEO metadata for a Candi page class.
 * Values from this annotation provide static defaults that can be
 * overridden at runtime by {@link SeoField} annotations on fields.
 *
 * <p>Usage:
 * <pre>
 * {@literal @}Page("/about")
 * {@literal @}Seo(title = "About Us", description = "Learn more about our company")
 * public class AboutPage { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Seo {

    /** Page title. Falls back to class simple name if empty. */
    String title() default "";

    /** Meta description. */
    String description() default "";

    /** Open Graph image URL. */
    String image() default "";

    /** Open Graph type (e.g. "website", "article"). */
    String type() default "website";

    /** If true, adds {@code <meta name="robots" content="noindex">}. */
    boolean noindex() default false;
}
