package net.voidhttp.response.cookie;

import net.voidhttp.header.Headers;

import java.util.List;

/**
 * Represents a HTTP response cookie manager.
 */
public interface Cookies {
    /**
     * Get a cookie with the given name;
     * @param name cookie name
     * @return the cookie
     */
    Cookie get(String name);

    /**
     * Add a cookie to the response.
     * @param cookie the cookie to add
     */
    void add(Cookie cookie);

    /**
     * Remove a cookie with the given name from the response.
     * @param name cookie name
     * @return cookie was removed
     */
    boolean remove(String name);

    /**
     * Remove a cookie from the response.
     * @param cookie cookie to remove
     * @return cookie was removed
     */
    boolean remove(Cookie cookie);

    /**
     * Invalidate a client cookie.
     * @param name cookie name
     */
    void invalidate(String name);

    /**
     * Invalidate a client cookie.
     * @param cookie cookie to invalidate
     */
    void invalidate(Cookie cookie);

    /**
     * Get the list of the cookies to be set.
     * @return cookie list
     */
    List<Cookie> getCookies();

    /**
     * Write cookies to the response headers.
     * @param headers response headers
     */
    void write(Headers headers);
}
