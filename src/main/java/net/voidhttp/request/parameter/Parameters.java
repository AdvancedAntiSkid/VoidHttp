package net.voidhttp.request.parameter;

/**
 * Represents a manager of HTTP request parameters.
 */
public interface Parameters {
    /**
     * Determine if the parameter key exists.
     * @param key parameter key
     * @return parameter exists
     */
    boolean has(String key);

    /**
     * Get the value of the given parameter.
     * @param key parameter key
     * @return parameter value
     */
    String get(String key);

    /**
     * Get the value of the given parameter or a
     * default value if the parameter is missing.
     * @param key parameter key
     * @param defaultValue default value to return if the parameter is missing
     * @return parameter value
     */
    String getOrDefault(String key, String defaultValue);
}
