package candi.web.seo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as providing dynamic SEO metadata at runtime.
 * The field value is read via reflection after the page handler executes.
 *
 * <p>Usage:
 * <pre>
 * {@literal @}Page("/post/{id}")
 * public class PostPage {
 *     {@literal @}SeoField(SeoRole.TITLE) private String title;
 *     {@literal @}SeoField(SeoRole.DESCRIPTION) private String excerpt;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SeoField {

    /** The SEO role this field provides. */
    SeoRole value();
}
