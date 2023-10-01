package net.voidhttp.header;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Represents a HTTP header manager.
 */
public interface Headers {
    /**
     * Determine if a header exists with the given key.
     * @param key header key
     * @return header exists
     */
    boolean has(String key);

    /**
     * Get the header with the given key.
     * @param key header key
     */
    String get(String key);

    /**
     * Get the map of the holding headers.
     * @return header list
     */
    Map<String, String> getHeaders();

    /**
     * Register a new header.
     * @param key header key
     * @param value header value
     */
    void add(String key, Object value);

    /**
     * Register a new header if the key is already set.
     * @param key header key
     * @param value header value
     */
    void addIfPresent(String key, Object value);

    /**
     * Register a new header if the key is missing.
     * @param key header key
     * @param value header value
     */
    void addIfAbsent(String key, Object value);

    /**
     * Register a new header.
     * @param header header to register
     */
    void add(Header header);

    /**
     * Remove a header from the registry.
     * @param key header key
     * @return header was removed
     */
    boolean remove(String key);

    /**
     * Write the registered headers to a print stream.
     * @param writer stream writer
     */
    void write(PrintWriter writer);
}
