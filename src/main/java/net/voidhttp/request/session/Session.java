package net.voidhttp.request.session;

/**
 * Represents a per-client cookie based request session.
 */
public interface Session {
    /**
     * Determine if the given key exists.
     * @param key data key
     * @return data exists
     */
    boolean has(String key);

    /**
     * Get the value of the key.
     * @param key data key
     * @return data value
     */
    <T> T get(String key);

    /**
     * Get the value of the key or a default value if key does not exists.
     * @param key data key
     * @param defaultValue value to return if key is missing
     * @return data value or default value
     */
    <T> T getOrDefault(String key, T defaultValue);

    /**
     * Set a session data.
     * @param key data key
     * @param value data value
     */
    <T> T set(String key, T value);

    /**
     * Set a session data if the key is taken.
     * @param key data key
     * @param value data value
     */
    <T> T setIfPresent(String key, T value);

    /**
     * Set a session data if the key is missing.
     * @param key data key
     * @param value data value
     */
    <T> T setIfAbsent(String key, T value);

    /**
     * Remove a session data.
     * @param key data key
     * @return any data removed
     */
    <T> T remove(String key);
}
