package net.voidhttp.handler;

import net.voidhttp.request.Request;
import net.voidhttp.request.query.Query;
import net.voidhttp.request.query.RequestQuery;
import net.voidhttp.response.Response;

import java.util.HashMap;
import java.util.Map;

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
    public boolean test(String url, Query query) {
        // the registry of the url query
        Map<String, String> queryData = new HashMap<>();
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
            // check if the part is a query placeholder
            if (routePart.startsWith(":")) {
                queryData.put(routePart.substring(1), urlPart);
                continue;
            }
            // test if the current part does not match the registered one
            if (!routePart.equals(urlPart))
                return false;
        }
        // apply query data
        RequestQuery requestQuery = (RequestQuery) query;
        for (Map.Entry<String, String> entry : queryData.entrySet())
            requestQuery.set(entry.getKey(), entry.getValue());
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

    /**
     * The request route url;
     */
    public String getRoute() {
        return route;
    }

    /**
     * The array of request handlers.
     */
    public Handler[] getHandlers() {
        return handlers;
    }
}
