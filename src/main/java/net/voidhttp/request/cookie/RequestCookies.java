package net.voidhttp.request.cookie;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a HTTP request cookie manager.
 */
public class RequestCookies implements Cookies {
    /**
     * The registry of the requested cookies.
     */
    private final Map<String, String> cookies;

    /**
     * Initialize request cookies.
     * @param cookies cookies registry
     */
    public RequestCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    /**
     * Determine if a cookie with the given name exists.
     * @param name cookie name
     * @return cookie exists
     */
    @Override
    public boolean has(String name) {
        return cookies.containsKey(name);
    }

    /**
     * Get the value of the cookie with the given name.
     * @param name cookie name
     * @return cookie value
     */
    @Override
    public String get(String name) {
        return cookies.get(name);
    }

    /**
     * Get the value of the cookie with the given name or
     * the default value if the cookie is missing.
     * @param name cookie name
     * @param defaultValue value to return if cookie is missing
     * @return cookie value
     */
    @Override
    public String getOrDefault(String name, String defaultValue) {
        return cookies.getOrDefault(name, defaultValue);
    }

    /**
     * Debug the request cookies.
     */
    @Override
    public String toString() {
        return "RequestCookies{" +
            "cookies=" + cookies +
            '}';
    }

    /**
     * Parse the requested cookies from header value.
     * @param header cookies header
     * @return parsed cookies
     */
    public static Cookies parse(String header) {
        // declare a map for parsed cookies
        Map<String, String> cookies = new HashMap<>();
        // cookies are separated using a semicolon;
        // split up the header between semicolons
        for (String cookie : header.split("; ")) {
            // split the key from the value
            String[] values = cookie.split("=");
            // register the cookie
            cookies.put(values[0], values[1]);
        }
        // create new cookies
        return new RequestCookies(cookies);
    }

    /**
     * Create an empty registry of cookies.
     * @return empty cookies registry
     */
    public static Cookies empty() {
        return new RequestCookies(new HashMap<>());
    }
}
