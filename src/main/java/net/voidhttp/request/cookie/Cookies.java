package net.voidhttp.request.cookie;

/**
 * Represents an HTTP request cookie manager.
 */
public interface Cookies {
    /**
     * Determine if a cookie with the given name exists.
     * @param name cookie name
     * @return cookie exists
     */
    boolean has(String name);

    /**
     * Get the value of the cookie with the given name.
     * @param name cookie name
     * @return cookie value
     */
    String get(String name);

    /**
     * Get the value of the cookie with the given name or
     * the default value if the cookie is missing.
     * @param name cookie name
     * @param defaultValue value to return if cookie is missing
     * @return cookie value
     */
    String getOrDefault(String name, String defaultValue);
}
