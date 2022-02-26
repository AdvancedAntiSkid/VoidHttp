package net.voidhttp;

import net.voidhttp.request.Request;
import net.voidhttp.response.Response;

/**
 * Represents a holder of HTTP request handlers that calls
 * the handlers if the route passes the test.
 */
public class Route {
    /**
     * The request route url;
     */
    private final String route;

    /**
     * The array of request handlers.
     */
    private final Handler[] handlers;

    /**
     * Initialize request route.
     * @param route route url
     * @param handlers request handlers
     */
    public Route(String route, Handler[] handlers) {
        this.route = route;
        this.handlers = handlers;
    }

    /**
     * Test if the request url matches the route url.
     * @param url request url
     * @return true if the url passed the test
     */
    public boolean test(String url) {
        // split up url between the '/' chars
        String[] routeParts = route.split("/");
        String[] urlParts = url.split("/");
        // test if the length of url parts does not match
        if (routeParts.length != urlParts.length)
            return false;
        // test the parts of the url
        for (int i = 0; i < routeParts.length; i++) {
            // get the current route and url part
            String routePart = routeParts[i];
            String urlPart = urlParts[i];
            // continue if the current part is an empty string
            if (routePart.isEmpty() && urlPart.isEmpty())
                continue;
            // continue if the current part is a placeholder,
            // thus starts with a colon
            if (routePart.startsWith(":"))
                continue;
            // test if the current part does not match the registered one
            if (!routePart.equals(urlPart))
                return false;
        }
        // url passed the test
        return true;
    }

    /**
     * Handle the HTTP request.
     * @param request client request
     * @param response server response
     */
    public void handle(Request request, Response response) {
        // loop through the registered request handlers
        for (Handler handler : handlers) {
            try {
                // handle the HTTP request
                handler.handle(request, response);
                // stop handling if the handler did not respond to the request
                if (!request.passed())
                    return;
            }
            // handle an exception occurred whilst handling the HTTP request
            catch (Exception e) {
                // TODO send an error response
                e.printStackTrace();
            }
        }
    }
}
