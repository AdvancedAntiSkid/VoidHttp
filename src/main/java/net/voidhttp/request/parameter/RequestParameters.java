package net.voidhttp.request.parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a manager of HTTP request parameters.
 */
public class RequestParameters implements Parameters {
    /**
     * The registry of the request url parameters.
     */
    private final Map<String, String> parameters;

    /**
     * Initialize request parameters.
     * @param parameters url parameters
     */
    public RequestParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Determine if the parameter key exists.
     * @param key parameter key
     * @return parameter exists
     */
    @Override
    public boolean has(String key) {
        return parameters.containsKey(key);
    }

    /**
     * Get the value of the given parameter.
     * @param key parameter key
     * @return parameter value
     */
    @Override
    public String get(String key) {
        return parameters.get(key);
    }

    /**
     * Get the value of the given parameter or a
     * default value if the parameter is missing.
     * @param key parameter key
     * @param defaultValue default value to return if the parameter is missing
     * @return parameter value
     */
    @Override
    public String getOrDefault(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    /**
     * Debug the request parameters.
     */
    @Override
    public String toString() {
        return "RequestParameters{" +
            "parameters=" + parameters +
            '}';
    }

    /**
     * Parse the request url parameters.
     * @param url request url
     * @return parsed parameters
     */
    public static Parameters parse(String url) {
        // declare a map for parsed parameters
        Map<String, String> parameters = new HashMap<>();
        // parameters are separated using a ampersand;
        // split up the header between ampersands
        for (String parameter : url.split("&")) {
            // split the key from the value
            String[] values = parameter.split("=");
            // register the parameter
            parameters.put(values[0], values.length > 1 ? values[1] : null);
        }
        // create new parameters
        return new RequestParameters(parameters);
    }

    /**
     * Create an empty registry of parameters.
     * @return empty parameters registry
     */
    public static Parameters empty() {
        return new RequestParameters(new HashMap<>());
    }
}
