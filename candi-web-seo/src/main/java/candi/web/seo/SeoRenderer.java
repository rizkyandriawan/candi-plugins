package candi.web.seo;

/**
 * Utility class that generates HTML meta tags from {@link SeoMeta}.
 * Produces {@code <title>}, Open Graph, Twitter Card, and robots meta tags.
 */
public final class SeoRenderer {

    private SeoRenderer() {
        // static utility class
    }

    /**
     * Render SEO meta tags as an HTML string.
     *
     * @param meta the resolved SEO metadata
     * @return HTML string containing all applicable meta tags
     */
    public static String render(SeoMeta meta) {
        if (meta == null) {
            return "";
        }

        StringBuilder html = new StringBuilder();

        // <title>
        if (meta.title() != null && !meta.title().isEmpty()) {
            html.append("<title>");
            html.append(escapeHtml(meta.title()));
            html.append("</title>\n");
        }

        // Meta description
        if (meta.description() != null && !meta.description().isEmpty()) {
            html.append("<meta name=\"description\" content=\"");
            html.append(escapeAttr(meta.description()));
            html.append("\">\n");
        }

        // Canonical URL
        if (meta.url() != null && !meta.url().isEmpty()) {
            html.append("<link rel=\"canonical\" href=\"");
            html.append(escapeAttr(meta.url()));
            html.append("\">\n");
        }

        // Open Graph tags
        if (meta.title() != null && !meta.title().isEmpty()) {
            html.append("<meta property=\"og:title\" content=\"");
            html.append(escapeAttr(meta.title()));
            html.append("\">\n");
        }

        if (meta.description() != null && !meta.description().isEmpty()) {
            html.append("<meta property=\"og:description\" content=\"");
            html.append(escapeAttr(meta.description()));
            html.append("\">\n");
        }

        if (meta.type() != null && !meta.type().isEmpty()) {
            html.append("<meta property=\"og:type\" content=\"");
            html.append(escapeAttr(meta.type()));
            html.append("\">\n");
        }

        if (meta.url() != null && !meta.url().isEmpty()) {
            html.append("<meta property=\"og:url\" content=\"");
            html.append(escapeAttr(meta.url()));
            html.append("\">\n");
        }

        if (meta.image() != null && !meta.image().isEmpty()) {
            html.append("<meta property=\"og:image\" content=\"");
            html.append(escapeAttr(meta.image()));
            html.append("\">\n");
        }

        if (meta.siteName() != null && !meta.siteName().isEmpty()) {
            html.append("<meta property=\"og:site_name\" content=\"");
            html.append(escapeAttr(meta.siteName()));
            html.append("\">\n");
        }

        // Twitter Card tags
        html.append("<meta name=\"twitter:card\" content=\"");
        html.append(meta.image() != null && !meta.image().isEmpty() ? "summary_large_image" : "summary");
        html.append("\">\n");

        if (meta.title() != null && !meta.title().isEmpty()) {
            html.append("<meta name=\"twitter:title\" content=\"");
            html.append(escapeAttr(meta.title()));
            html.append("\">\n");
        }

        if (meta.description() != null && !meta.description().isEmpty()) {
            html.append("<meta name=\"twitter:description\" content=\"");
            html.append(escapeAttr(meta.description()));
            html.append("\">\n");
        }

        if (meta.image() != null && !meta.image().isEmpty()) {
            html.append("<meta name=\"twitter:image\" content=\"");
            html.append(escapeAttr(meta.image()));
            html.append("\">\n");
        }

        // Robots noindex
        if (meta.noindex()) {
            html.append("<meta name=\"robots\" content=\"noindex\">\n");
        }

        return html.toString();
    }

    /**
     * Escape HTML content (for use between tags).
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Escape a string for use in an HTML attribute value.
     */
    private static String escapeAttr(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#x27;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
