package candi.web.seo;

/**
 * Holds resolved SEO metadata for a page request.
 * Built by {@link SeoInterceptor} and consumed by {@link SeoRenderer}
 * and the {@link CndSeoWidget}.
 */
public record SeoMeta(
        String title,
        String description,
        String image,
        String url,
        String type,
        boolean noindex,
        String siteName
) {

    /** Request attribute key used to store SeoMeta. */
    public static final String REQUEST_ATTRIBUTE = "candi.seo.meta";
}
