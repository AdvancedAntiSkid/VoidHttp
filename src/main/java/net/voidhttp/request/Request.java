package net.voidhttp.request;

import com.google.gson.JsonObject;
import net.voidhttp.header.Headers;
import net.voidhttp.request.cookie.Cookies;
import net.voidhttp.request.parameter.Parameters;

import java.net.InetAddress;

/**
 * Represents a client HTTP request.
 */
public interface Request {
    /**
     * Get the requested url.
     */
    String getRoute();

    /**
     * Get the HTTP request method used.
     */
    Method getMethod();

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
    InetAddress getHost();

    /**
     * Get the registry of the requested headers.
     */
    Headers headers();

    /**
     * Get the registry of the request cookies.
     */
    Cookies cookies();

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
}
