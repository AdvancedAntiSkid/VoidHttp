package net.voidhttp.request;

import com.google.gson.JsonObject;
import net.voidhttp.header.Headers;
import net.voidhttp.request.cookie.Cookies;
import net.voidhttp.request.data.Data;
import net.voidhttp.request.parameter.Parameters;
import net.voidhttp.request.query.Query;
import net.voidhttp.request.session.Session;

import java.net.InetAddress;

/**
 * Represents a client HTTP request.
 */
public interface Request {
    /**
     * Get the requested url.
     */
    String route();

    /**
     * Get the HTTP request method used.
     */
    Method method();

    /**
     * The current handler has passed executing.
     * Mark the request as done.
     */
    void next();

    /**
     * Reset the request pass state.
     */
    void reset();

    /**
     * Determine if the handler has passed executing.
     */
    boolean passed();

    /**
     * Get the address of the requesting client.
     * @return ip address
     */
    InetAddress host();

    /**
     * Get the registry of the requested headers.
     */
    Headers headers();

    /**
     * Get the registry of the request cookies.
     */
    Cookies cookies();

    /**
     * Get the registry of the passed values.
     */
    Data data();

    /**
     * Get the request body content.
     */
    String body();

    /**
     * Get the request body json.
     */
    JsonObject json();

    /**
     * Get the request parameters.
     */
    Parameters parameters();

    /**
     * Get the current request session.
     */
    Session session();

    /**
     * The query data of the url.
     */
    Query query();

    /**
     * Set the current request session.
     */
    void setSession(Session session);
}
