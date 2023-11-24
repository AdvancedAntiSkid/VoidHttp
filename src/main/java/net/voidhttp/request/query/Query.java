package net.voidhttp.request.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a holder of the request url query.
 */
public interface Query {
    /**
     * Determine if the query key exists.
     * @param key query key
     * @return query exists
     */
    boolean has(@NotNull String key);

    /**
     * Get the value of the given query key.
     * @param key query key
     * @return query value
     */
    @Nullable String get(@NotNull String key);

    /**
     * Get the value of the given query key or a
     * default value if the query key is missing.
     * @param key query key
     * @param defaultValue default value to return if the query key is missing
     * @return query value
     */
    @NotNull String getOrDefault(@NotNull String key, @NotNull String defaultValue);
}
