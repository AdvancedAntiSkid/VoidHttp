package net.voidhttp.util;

/**
 * Represents a key-value holder that is passed for template rendering.
 */
public class Placeholder {
    /**
     * The placeholder key to be replaced.
     */
    private final String key;

    /**
     * The placeholder value to replace with.
     */
    private final String value;

    /**
     * Initialize template placeholder.
     * @param key placeholder key
     * @param value placeholder value
     */
    public Placeholder(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Get the placeholder key to be replaced.
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the placeholder value to replace with.
     */
    public String getValue() {
        return value;
    }
}
