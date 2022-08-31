package net.voidhttp.request.query;

/**
 * Represents a holder of the request url query.
 */
public interface Query {
    /**
     * Determine if the query key exists.
     * @param key query key
     * @return query exists
     */
    boolean has(String key);

    /**
     * Get the value of the given query key.
     * @param key query key
     * @return query value
     */
    String get(String key);

    /**
     * Get the value of the given query key or a
     * default value if the query key is missing.
     * @param key query key
     * @param defaultValue default value to return if the query key is missing
     * @return query value
     */
    String getOrDefault(String key, String defaultValue);
}
