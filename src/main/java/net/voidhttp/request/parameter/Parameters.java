package net.voidhttp.request.parameter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a manager of HTTP request parameters.
 */
public interface Parameters {
    /**
     * Determine if the parameter key exists.
     * @param key parameter key
     * @return parameter exists
     */
    boolean has(@NotNull String key);

    /**
     * Get the value of the given parameter.
     * @param key parameter key
     * @return parameter value
     */
    @Nullable String get(@NotNull String key);

    /**
     * Get the value of the given parameter or a
     * default value if the parameter is missing.
     * @param key parameter key
     * @param defaultValue default value to return if the parameter is missing
     * @return parameter value
     */
    @NotNull String getOrDefault(@NotNull String key, @NotNull String defaultValue);
}
