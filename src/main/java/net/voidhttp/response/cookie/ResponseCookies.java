package net.voidhttp.response.cookie;

import net.voidhttp.header.Headers;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a HTTP response cookie manager.
 */
public class ResponseCookies implements Cookies {
    /**
     * The list of cookies to be set in the response.
     */
    private final List<Cookie> cookies;

    /**
     * Initialize response cookies.
     * @param cookies cookies to be set
     */
    public ResponseCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    /**
     * Initialize response cookies.
     */
    public ResponseCookies() {
        this(new ArrayList<>());
    }

    /**
     * Get a cookie with the given name;
     * @param name cookie name
     * @return the cookie
     */
    @Override
    public Cookie get(String name) {
        return cookies.stream().filter(cookie -> cookie.getName().equals(name))
            .findFirst().orElse(null);
    }

    /**
     * Add a cookie to the response.
     * @param cookie the cookie to add
     */
    @Override
    public void add(Cookie cookie) {
        cookies.add(cookie);
    }

    /**
     * Remove a cookie with the given name from the response.
     * @param name cookie name
     * @return cookie was removed
     */
    @Override
    public boolean remove(String name) {
        return cookies.removeIf(cookie -> cookie.getName().equals(name));
    }

    /**
     * Remove a cookie from the response.
     * @param cookie cookie to remove
     * @return cookie was removed
     */
    @Override
    public boolean remove(Cookie cookie) {
        return cookies.remove(cookie);
    }

    /**
     * Get the list of the cookies to be set.
     * @return cookie list
     */
    @Override
    public List<Cookie> getCookies() {
        return cookies;
    }

    /**
     * Write cookies to the response headers.
     * @param headers response headers
     */
    @Override
    public void write(Headers headers) {
        // add the response cookies to the response headers
        for (Cookie cookie : cookies)
            headers.add("Set-Cookie", cookie.parse());
    }
}
