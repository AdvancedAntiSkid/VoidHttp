package net.voidhttp.response.cookie;

import lombok.RequiredArgsConstructor;
import net.voidhttp.header.Headers;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an HTTP response cookie manager.
 */
@RequiredArgsConstructor
public class ResponseCookies implements Cookies {
    /**
     * The list of cookies to be set in the response.
     */
    private final List<Cookie> cookies;

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
        return cookies
            .stream()
            .filter(cookie -> cookie.getName().equals(name))
            .findFirst()
            .orElse(null);
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
     * Invalidate a client cookie.
     * @param name cookie name
     */
    @Override
    public void invalidate(String name) {
        add(new Cookie(name, "").setMaxAge(0));
    }

    /**
     * Invalidate a client cookie.
     * @param cookie cookie to invalidate
     */
    @Override
    public void invalidate(Cookie cookie) {
        invalidate(cookie.getName());
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
