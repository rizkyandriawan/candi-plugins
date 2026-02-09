package candi.web.routes;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static accessor for all registered page routes.
 * Populated at startup by {@link RoutesRegistry}.
 *
 * <p>Usage: {@code Routes.get("EDIT_POST").url(42)} returns {@code "/posts/42/edit"}
 */
public class Routes {

    private static final Map<String, Route> routes = new ConcurrentHashMap<>();

    private Routes() {
        // static utility class
    }

    /**
     * Get a route by its name.
     *
     * @param name the route name (e.g. "EDIT_POST")
     * @return the Route, or null if not found
     */
    public static Route get(String name) {
        return routes.get(name);
    }

    /**
     * Get an unmodifiable view of all registered routes.
     */
    public static Map<String, Route> all() {
        return Collections.unmodifiableMap(routes);
    }

    /**
     * Register a route. Called by {@link RoutesRegistry} at startup.
     */
    static void register(String name, Route route) {
        routes.put(name, route);
    }

    /**
     * Clear all routes. Used for testing and hot reload.
     */
    static void clear() {
        routes.clear();
    }
}
