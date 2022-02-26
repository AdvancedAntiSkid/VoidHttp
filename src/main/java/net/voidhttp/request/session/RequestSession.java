package net.voidhttp.request.session;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a per-client cookie based request session.
 */
public class RequestSession implements Session {
    /**
     * The session data map.
     */
    private final Map<String, Object> data;

    /**
     * Initialize request session.
     * @param data session data
     */
    public RequestSession(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * Initialize request session.
     */
    public RequestSession() {
        this(new HashMap<>());
    }

    /**
     * Determine if the given key exists.
     * @param key data key
     * @return data exists
     */
    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    /**
     * Get the value of the key.
     * @param key data key
     * @return data value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    /**
     * Get the value of the key or a default value if key does not exists.
     * @param key data key
     * @param defaultValue value to return if key is missing
     * @return data value or default value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    /**
     * Set a session data.
     * @param key data key
     * @param value data value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T set(String key, T value) {
        return (T) data.put(key, value);
    }

    /**
     * Set a session data if the key is taken.
     * @param key data key
     * @param value data value
     */
    @Override
    public <T> T setIfPresent(String key, T value) {
        if (get(key) != null)
            return set(key, value);
        return null;
    }

    /**
     * Set a session data if the key is missing.
     * @param key data key
     * @param value data value
     */
    @Override
    public <T> T setIfAbsent(String key, T value) {
        if (get(key) == null)
            return set(key, value);
        return null;
    }

    /**
     * Remove a session data.
     * @param key data key
     * @return any data removed
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T remove(String key) {
        return (T) data.remove(key);
    }
}
