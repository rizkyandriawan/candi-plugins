package candi.web.routes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A named route that supports type-safe URL building.
 * Replaces {@code {param}} placeholders with provided argument values.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code new Route("/posts").url()} returns {@code "/posts"}</li>
 *   <li>{@code new Route("/posts/{id}").url(42)} returns {@code "/posts/42"}</li>
 *   <li>{@code new Route("/posts/{id}/edit").url(42)} returns {@code "/posts/42/edit"}</li>
 * </ul>
 */
public class Route {

    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{[^}]+}");

    private final String pattern;

    public Route(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Build the URL with no parameters. Returns the raw pattern.
     */
    public String url() {
        return pattern;
    }

    /**
     * Build the URL by replacing {@code {param}} placeholders in order
     * with the provided arguments.
     *
     * @param params values to substitute for path parameters, in order
     * @return the resolved URL path
     */
    public String url(Object... params) {
        if (params == null || params.length == 0) {
            return pattern;
        }

        Matcher matcher = PARAM_PATTERN.matcher(pattern);
        StringBuilder result = new StringBuilder();
        int paramIndex = 0;

        while (matcher.find()) {
            String replacement = paramIndex < params.length
                    ? String.valueOf(params[paramIndex++])
                    : matcher.group();
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Get the raw path pattern (e.g. "/posts/{id}/edit").
     */
    public String getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return "Route(" + pattern + ")";
    }
}
