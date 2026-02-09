package candi.web.routes;

import org.springframework.stereotype.Component;

/**
 * Template helper bean for accessing routes in Candi templates.
 * Registered as a Spring bean named "routes" so it can be used directly
 * in template expressions.
 *
 * <p>Usage in templates: {@code {{ routes.get("EDIT_POST").url(post.id) }}}
 */
@Component("routes")
public class RoutesTemplateHelper {

    /**
     * Get a route by name.
     *
     * @param name the route name (e.g. "EDIT_POST")
     * @return the Route, or null if not found
     */
    public Route get(String name) {
        return Routes.get(name);
    }
}
