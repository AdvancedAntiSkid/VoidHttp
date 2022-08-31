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
     * Initialize http header.
     * @param key header key
     * @param value header value
     */
    public Header(String key, String value) {
        this.key = key.toLowerCase();
        this.value = value;
    }

    /**
     * Initialize http header.
     * @param key header key
     * @param value header value
     */
    public Header(String key, Object value) {
        this.key = key.toLowerCase();
        this.value = String.valueOf(value);
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

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof Header) {
            return ((Header) other).key.equals(key);
        } else if (other instanceof String) {
            return is((String) other);
        }
        return false;
    }

    public boolean is(String key) {
        return this.key.equals(key.toLowerCase());
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
