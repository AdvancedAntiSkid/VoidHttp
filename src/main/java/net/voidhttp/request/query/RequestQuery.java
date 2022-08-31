package net.voidhttp.request.query;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a holder of the request url query.
 */
public class RequestQuery implements Query {
    /**
     * The registry of the request url query placeholders.
     */
    private final Map<String, String> query;

    /**
     * Initialize request query.
     * @param query query data
     */
    public RequestQuery(Map<String, String> query) {
        this.query = query;
    }

    /**
     * Initialize request query.
     */
    public RequestQuery() {
        this(new HashMap<>());
    }

    /**
     * Determine if the query key exists.
     * @param key query key
     * @return query exists
     */
    @Override
    public boolean has(String key) {
        return query.containsKey(key);
    }

    /**
     * Update the value of the given key.
     * @param key key to update
     * @param value key value
     */
    public void set(String key, String value) {
        query.put(key, value);
    }

    /**
     * Get the value of the given query key.
     * @param key query key
     * @return query value
     */
    @Override
    public String get(String key) {
        return query.get(key);
    }

    /**
     * Get the value of the given query key or a
     * default value if the query key is missing.
     * @param key query key
     * @param defaultValue default value to return if the query key is missing
     * @return query value
     */
    @Override
    public String getOrDefault(String key, String defaultValue) {
        return query.getOrDefault(key, defaultValue);
    }

    @Override
    public String toString() {
        return "RequestQuery{" +
            "query=" + query +
            '}';
    }
}
