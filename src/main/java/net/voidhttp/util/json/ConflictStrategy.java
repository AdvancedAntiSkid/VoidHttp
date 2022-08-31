package net.voidhttp.util.json;

/**
 * Represents a holder of strategies to use when a merging conflict occurs:
 * the key exists in both the source and the update.
 */
public enum ConflictStrategy {
    /**
     * Keep the original value, do not modify anything.
     */
    KEEP_ORIGINAL,

    /**
     * Force override the original value to the update value.
     */
    OVERRIDE,

    /**
     * Override the original value if the update value is not null.
     */
    OVERRIDE_NOT_NULL,

    /**
     * Throw an exception if the update key already exists in the source.
     */
    THROW_EXCEPTION,
}