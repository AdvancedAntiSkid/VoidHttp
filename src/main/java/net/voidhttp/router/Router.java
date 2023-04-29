package net.voidhttp.router;

import net.voidhttp.HttpServer;
import net.voidhttp.config.Flag;
import net.voidhttp.request.HttpRequest;
import net.voidhttp.request.Method;
import net.voidhttp.response.HttpResponse;

import java.io.IOException;
import java.util.*;

/**
 * Represents a per-method routes' handler.
 */
public class Router {
    /**
     * The http server of the router that accepts incoming client requests.
     */
    private final HttpServer server;

    /**
     * The map of the registered HTTP routes.
     */
    private final Map<Method, List<Route>> routeMap = new HashMap<>();

    /**
     * The map of the registered error routes.
     */
    private final Map<Integer, List<Route>> errorMap = new HashMap<>();

    /**
     * The list of the globally used handlers.
     */
    private final List<Middleware> middlewares = new ArrayList<>();

    /**
     * Initialize the request router.
     * @param server http server
     */
    public Router(HttpServer server) {
        this.server = server;
    }

    /**
     * Register a handler for the given request method.
     * @param method request method
     * @param route request route
     * @param middlewares request handlers
     */
    public void register(Method method, String route, Middleware... middlewares) {
        // get the registered routes for the method
        List<Route> routes = routeMap.getOrDefault(method, new ArrayList<>());
        // register the handlers
        routes.add(new Route(route, middlewares));
        // update the routes
        routeMap.put(method, routes);
    }

    /**
     * Register a request error handler.
     * @param code error code
     * @param middlewares error handlers
     */
    public void error(int code, Middleware... middlewares) {
        // get the registered routes for the error code
        List<Route> routes = errorMap.getOrDefault(code, new ArrayList<>());
        // register the handlers
        routes.add(new Route("", middlewares));
        // update the routes
        errorMap.put(code, routes);
    }

    /**
     * Register a global request handler.
     * @param middlewares global handlers
     */
    public void use(Middleware... middlewares) {
        this.middlewares.addAll(Arrays.asList(middlewares));
    }

    /**
     * Preprocess the HTTP request and call middlewares.
     * @param context http request execution context
     */
    public void preprocess(Context context) throws IOException {
        // extract the request and response of the context
        HttpRequest request = context.getRequest();
        HttpResponse response = context.getResponse();
        // handle globally used middlewares
        // this must be done before handling the method handlers
        for (Middleware middleware : middlewares) {
            try {
                // make the global handler handle the request
                middleware.handle(request, response);
                // stop processing if the handler did not pass the handling
                if (!request.passed())
                    return;
                request.reset();
            }
            // handle an occurred error happened whilst
            // processing global middleware
            catch (Exception e) {
                // retrieve the stack trace to the client
                handleError(context, e);
                return;
            }
        }
    }

    /**
     * Handle a 404 not found request of the given method and url.
     * @param context http request execution context
     */
    public void handleNotFound(Context context) {
        // extract the request and response of the context
        HttpRequest request = context.getRequest();
        HttpResponse response = context.getResponse();
        // get the code 404 handlers
        boolean handled = false;
        List<Route> errorRoutes = errorMap.get(404);
        // check if there are any of them
        if (errorRoutes != null) {
            // loop through the error handlers
            for (Route route : errorRoutes) {
                route.handle(request, response);
                // handle the 404 error
                handled = true;
                // stop processing if the handler did not pass the handling
                if (!request.passed())
                    return;
                request.reset();
            }
        }
        // check if the request was not handled and there
        // weren't any code 404 handlers
        if (!handled) {
            try {
                response.status(404).send("<pre>" + "Cannot " + context.getMethod() + " " + context.getUrl() + "</pre>");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleError(Context context, Throwable error) {
        error.printStackTrace();
        // extract the request and response of the context
        HttpRequest request = context.getRequest();
        HttpResponse response = context.getResponse();
        // get the code 400 handlers
        boolean handled = false;
        List<Route> errorRoutes = errorMap.get(400);
        // check if there are any of them
        if (errorRoutes != null) {
            // loop through the error handlers
            for (Route route : errorRoutes) {
                route.handle(request, response);
                // handle the 404 error
                handled = true;
                // stop processing if the handler did not pass the handling
                if (!request.passed())
                    return;
                request.reset();
            }
        }
        // check if the request was not handled and there
        // weren't any code 400 handlers
        if (!handled) {
            try {
                // check if no stack trace should be sent
                if (server.hasFlag(Flag.NO_STACK_TRACE)) {
                    response.status(400).send("");
                    return;
                }
                // send the stack trace to the client
                response.sendError(error);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Route> getRoutes(Method method) {
        return routeMap.get(method);
    }

    public List<Route> getErrorRoutes(int code) {
        return errorMap.get(code);
    }
}
