package net.voidhttp.header;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an HTTP header manager.
 */
public class HttpHeaders implements Headers {
    /**
     * The registered HTTP headers.
     */
    private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Initialize HTTP headers.
     * @param headers header registry
     */
    public HttpHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    /**
     * Determine if a header exists with the given key.
     * @param key header key
     * @return header exists
     */
    @Override
    public boolean has(String key) {
        return headers.containsKey(key);
    }

    /**
     * Get the header with the given key.
     * @param key header key
     */
    @Override
    public String get(String key) {
        return headers.get(key);
    }


    /**
     * Get the map of the holding headers.
     * @return header list
     */
    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Register a new header.
     * @param key header key
     * @param value header value
     */
    @Override
    public void add(String key, Object value) {
        headers.put(key, String.valueOf(value));
    }

    /**
     * Register a new header if the key is already set.
     * @param key header key
     * @param value header value
     */
    @Override
    public void addIfPresent(String key, Object value) {
        if (get(key) != null)
            add(key, value);
    }

    /**
     * Register a new header if the key is missing.
     * @param key header key
     * @param value header value
     */
    @Override
    public void addIfAbsent(String key, Object value) {
        if (get(key) == null)
            add(key, value);
    }

    /**
     * Remove a header from the registry.
     * @param key header key
     * @return header was removed
     */
    @Override
    public boolean remove(String key) {
        return headers.remove(key) != null;
    }

    /**
     * Write the registered headers to a print stream.
     * @param writer stream writer
     */
    @Override
    public void write(PrintWriter writer) {
        for (Map.Entry<String, String> entry : headers.entrySet())
            writer.print(entry.getKey() + ": " + entry.getValue() + "\r\n");
    }

    /**
     * Debug the HTTP headers.
     */
    @Override
    public String toString() {
        return "HttpHeaders{" +
            "headers=" + headers +
            '}';
    }

    /**
     * Parse the HTTP headers from raw data.
     * @param data raw header data
     * @return parsed headers
     */
    public static Headers parse(List<String> data) {
        // declare a list for parsed headers
        Map<String, String> headers = new HashMap<>();
        // loop through the raw headers data
        for (String header : data) {
            // get the index of the first colon
            int index = header.indexOf(':');
            // get the key and value of the header
            String key = header.substring(0, index);
            String value = header.substring(index + 2);
            // register the header
            headers.put(key, value);
        }
        // create new headers
        return new HttpHeaders(headers);
    }

    /**
     * Create an empty registry of HTTP headers.
     * @return empty header registry
     */
    public static Headers empty() {
        return new HttpHeaders(new HashMap<>());
    }
}
