package candi.web.seo;

/**
 * Defines the SEO role a field provides when annotated with {@link SeoField}.
 */
public enum SeoRole {
    /** The page title. */
    TITLE,
    /** The meta description. */
    DESCRIPTION,
    /** The Open Graph image URL. */
    IMAGE,
    /** The canonical URL. */
    CANONICAL
}
