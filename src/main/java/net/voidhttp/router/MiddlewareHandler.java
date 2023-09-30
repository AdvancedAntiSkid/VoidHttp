package net.voidhttp.router;

import net.voidhttp.request.Request;
import net.voidhttp.response.Response;

/**
 * Represents an HTTP middleware that attached to a route and
 * is called when the route passes the url test.
 */
public interface MiddlewareHandler {
    /**
     * Handle the incoming HTTP request.
     * @param req client request
     * @param res server response
     * @throws Exception server failed to handle
     */
    void handle(Request req, Response res) throws Exception;
}
