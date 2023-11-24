package net.voidhttp.request.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a request data manager that can be used to pass data between request handlers.
 */
public interface Data {
    /**
     * Determine if the given key exists.
     * @param key data key
     * @return data exists
     */
    boolean has(@NotNull String key);

    /**
     * Get a T value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @param <T> data type
     * @return data value
     */
    @NotNull <T> T get(@NotNull String key, @NotNull T defaultValue);

    /**
     * Get a T value from the data registry.
     * @param key data key
     * @param <T> data type
     * @return data value
     */
    @Nullable <T> T get(@NotNull String key);

    /**
     * Get a T value from the data registry.
     * @param key data key
     * @param type data type class
     * @param defaultValue value to return if the key is unregistered
     * @param <T> data type
     * @return data value
     */
    @NotNull <T> T get(@NotNull String key, @NotNull Class<T> type, @NotNull T defaultValue);

    /**
     * Get a T value from the data registry.
     * @param key data key
     * @param type data type class
     * @param <T> data type
     * @return data value
     */
    @Nullable <T> T get(@NotNull String key, @NotNull Class<T> type);

    /**
     * Get an object value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @NotNull Object getObject(@NotNull String key, @NotNull Object defaultValue);

    /**
     * Get an object value from the data registry.
     * @param key data key
     * @return data value
     */
   @Nullable Object getObject(@NotNull String key);

    /**
     * Get a string value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @NotNull String getString(@NotNull String key, @NotNull String defaultValue);

    /**
     * Get a string value from the data registry.
     * @param key data key
     * @return data value
     */
    @Nullable String getString(@NotNull String key);

    /**
     * Get a boolean value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    boolean getBoolean(@NotNull String key, boolean defaultValue);

    /**
     * Get a boolean value from the data registry.
     * @param key data key
     * @return data value
     */
    boolean getBoolean(@NotNull String key);

    /**
     * Get an int value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    int getInt(@NotNull String key, int defaultValue);

    /**
     * Get an int value from the data registry.
     * @param key data key
     * @return data value
     */
    int getInt(@NotNull String key);

    /**
     * Get a long value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    long getLong(@NotNull String key, long defaultValue);

    /**
     * Get a long value from the data registry.
     * @param key data key
     * @return data value
     */
    long getLong(@NotNull String key);

    /**
     * Get a float value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    float getFloat(@NotNull String key, float defaultValue);

    /**
     * Get a float value from the data registry.
     * @param key data key
     * @return data value
     */
    float getFloat(@NotNull String key);

    /**
     * Get a double value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    double getDouble(@NotNull String key, double defaultValue);

    /**
     * Get a double value from the data registry.
     * @param key data key
     * @return data value
     */
    double getDouble(@NotNull String key);

    /**
     * Set the value of the given key.
     * @param key data key
     * @param value data value
     * @param <T> data type
     * @return previous data stored with this key
     */
    <T> @Nullable T set(@NotNull String key, @NotNull T value);

    /**
     * Set the value of the given key if the key is already registered.
     * @param key data key
     * @param value data value
     * @param <T> data type
     * @return previous data stored with this key
     */
    <T> @Nullable T setIfPresent(@NotNull String key, @NotNull T value);

    /**
     * Set the value of the given key if the key is not registered yet.
     * @param key data key
     * @param value data value
     * @param <T> data type
     * @return previous data stored with this key
     */
    <T> @Nullable T setIfAbsent(@NotNull String key, @NotNull T value);

    /**
     * Remove a data from the registry.
     * @param key data key
     * @param <T> data type
     * @return removed data value
     */
    <T> @Nullable T remove(@NotNull String key);

    /**
     * Clear all the registered values.
     */
    void clear();
}
