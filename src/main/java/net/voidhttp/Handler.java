package net.voidhttp;

import net.voidhttp.request.Request;
import net.voidhttp.response.Response;

/**
 * Represents a HTTP middleware that attached to a route and
 * is called when the route passes the url test.
 */
public interface Handler {
    /**
     * Handle the incoming HTTP request.
     * @param req client request
     * @param res server response
     * @throws Exception server failed to handle
     */
    void handle(Request req, Response res) throws Exception;
}
