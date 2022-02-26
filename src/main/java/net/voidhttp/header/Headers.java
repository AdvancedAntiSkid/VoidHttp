package net.voidhttp.header;

import java.io.PrintWriter;
import java.util.List;

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
    Header get(String key);

    /**
     * Get the list of headers with the given key.
     * @param key header key
     * @return header list
     */
    List<Header> getAll(String key);

    /**
     * Get the list of the holding headers.
     * @return header list
     */
    List<Header> getHeaders();

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
     * Remove all the headers from the registry with the given key.
     * @param key header key
     * @return any headers were removed
     */
    boolean removeAll(String key);

    /**
     * Write the registered headers to a print stream.
     * @param writer stream writer
     */
    void write(PrintWriter writer);
}
