package net.voidhttp.handler;

import net.voidhttp.HttpServer;
import net.voidhttp.request.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a route collector blueprint.
 */
public class Blueprint {
    /**
     * The url prefix of the blueprint routes.
     */
    private final String prefix;

    /**
     * The map of the registered HTTP routes.
     */
    private final Map<Method, List<Route>> routeMap = new HashMap<>();

    /**
     * Initialize blueprint.
     * @param prefix url prefix
     */
    public Blueprint(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Register a handler for the given request method.
     * @param method request method
     * @param route request route
     * @param handlers request handlers
     */
    public Blueprint register(Method method, String route, Handler... handlers) {
        // get the registered routes for the method
        List<Route> routes = routeMap.getOrDefault(method, new ArrayList<>());
        // register the handlers
        routes.add(new Route(route, handlers));
        // update the routes
        routeMap.put(method, routes);
        return this;
    }

    /**
     * Register a GET request handler.
     * @param route request route
     * @param handlers request handlers
     */
    public Blueprint get(String route, Handler... handlers) {
        return register(Method.GET, route, handlers);
    }

    /**
     * Register a POST request handler.
     * @param route request route
     * @param handlers request handler
     */
    public Blueprint post(String route, Handler... handlers) {
        return register(Method.POST, route, handlers);
    }

    /**
     * Setup the blueprint for the server.
     * @param server server to setup to
     */
    public void setup(HttpServer server) {
        // loop through the registered HTTP methods
        for (Method method : routeMap.keySet()) {
            // get the list of routes registered for the method
            for (Route route : routeMap.get(method)) {
                // register the route handler and append the blueprint prefix
                server.register(method, prefix + route.getRoute(), route.getHandlers());
            }
        }
    }
}
