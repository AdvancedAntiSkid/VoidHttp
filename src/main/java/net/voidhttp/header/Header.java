package net.voidhttp.header;

/**
 * Represents a HTTP header manager.
 */
public class Header {
    /**
     * The key of the header.
     */
    private final String key;

    /**
     * The value of the header.
     */
    private final String value;

    /**
     * Initialize HTTP header.
     * @param key header key
     * @param value header value
     */
    public Header(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Get the key of the header.
     */
    public String key() {
        return key;
    }

    /**
     * Get the value of the header.
     */
    public String value() {
        return value;
    }

    /**
     * Debug the HTTP header.
     */
    @Override
    public String toString() {
        return "Header{" +
            "key='" + key + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
