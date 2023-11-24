package net.voidhttp.request.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RequestData implements Data {
    private final Map<String, Object> registry;

    public RequestData(Map<String, Object> registry) {
        this.registry = registry;
    }

    public RequestData() {
        this(new HashMap<>());
    }

    /**
     * Determine if the given key exists.
     * @param key data key
     * @return data exists
     */
    @Override
    public boolean has(@NotNull String key) {
        return registry.containsKey(key);
    }

    /**
     * Get a T value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> @NotNull T get(@NotNull String key, @NotNull T defaultValue) {
        return (T) registry.getOrDefault(key, defaultValue);
    }

    /**
     * Get a T value from the data registry.
     * @param key data key
     * @return data value
     */
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T> T get(@NotNull String key) {
        return (T) registry.get(key);
    }

    /**
     * Get a T value from the data registry.
     * @param key data key
     * @param type data type class
     * @param defaultValue value to return if the key is unregistered
     * @param <T> data type
     * @return data value
     */
    @Override
    public <T> @NotNull T get(@NotNull String key, @NotNull Class<T> type, @NotNull T defaultValue) {
        return type.cast(registry.getOrDefault(key, defaultValue));
    }

    /**
     * Get a T value from the data registry.
     * @param key data key
     * @param type data type class
     * @param <T> data type
     * @return data value
     */
    @Override
    public @Nullable <T> T get(@NotNull String key, @NotNull Class<T> type) {
        return type.cast(registry.get(key));
    }

    /**
     * Get an object value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public @NotNull Object getObject(@NotNull String key, @NotNull Object defaultValue) {
        return registry.getOrDefault(key, defaultValue);
    }

    /**
     * Get an object value from the data registry.
     * @param key data key
     * @return data value
     */
    @Override
    public @Nullable Object getObject(@NotNull String key) {
        return registry.get(key);
    }

    /**
     * Get a string value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public @NotNull String getString(@NotNull String key, @NotNull String defaultValue) {
        return (String) registry.getOrDefault(key, defaultValue);
    }

    /**
     * Get a string value from the data registry.
     * @param key data key
     * @return data value
     */
    @Override
    public @Nullable String getString(@NotNull String key) {
        return (String) registry.get(key);
    }

    /**
     * Get a boolean value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public boolean getBoolean(@NotNull String key, boolean defaultValue) {
        Object value = registry.getOrDefault(key, defaultValue);
        if (!(value instanceof Boolean))
            return false;
        return (Boolean) value;
    }

    /**
     * Get a boolean value from the data registry.
     * @param key data key
     * @return data value
     */
    @Override
    public boolean getBoolean(@NotNull String key) {
        return getBoolean(key, false);
    }

    /**
     * Get an int value from the data registry.
     * @param key          data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public int getInt(@NotNull String key, int defaultValue) {
        Object value = registry.getOrDefault(key, defaultValue);
        if (!(value instanceof Integer))
            return 0;
        return (Integer) value;
    }

    /**
     * Get an int value from the data registry.
     * @param key data key
     * @return data value
     */
    @Override
    public int getInt(@NotNull String key) {
        return getInt(key, 0);
    }

    /**
     * Get a long value from the data registry.
     * @param key          data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public long getLong(@NotNull String key, long defaultValue) {
        Object value = registry.getOrDefault(key, defaultValue);
        if (!(value instanceof Long))
            return 0L;
        return (Long) value;
    }

    /**
     * Get a long value from the data registry.
     * @param key data key
     * @return data value
     */
    @Override
    public long getLong(@NotNull String key) {
        return getLong(key, 0L);
    }

    /**
     * Get a float value from the data registry.
     * @param key          data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public float getFloat(@NotNull String key, float defaultValue) {
        Object value = registry.getOrDefault(key, defaultValue);
        if (!(value instanceof Float))
            return 0F;
        return (Float) value;
    }

    /**
     * Get a float value from the data registry.
     * @param key data key
     * @return data value
     */
    @Override
    public float getFloat(@NotNull String key) {
        return getFloat(key, 0F);
    }

    /**
     * Get a double value from the data registry.
     * @param key          data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public double getDouble(@NotNull String key, double defaultValue) {
        Object value = registry.getOrDefault(key, defaultValue);
        if (!(value instanceof Double))
            return 0D;
        return (Double) value;
    }

    /**
     * Get a double value from the data registry.
     * @param key data key
     * @return data value
     */
    @Override
    public double getDouble(@NotNull String key) {
        return getDouble(key, 0D);
    }

    /**
     * Set the value of the given key.
     * @param key   data key
     * @param value data value
     * @return previous data stored with this key
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T set(@NotNull String key, @NotNull T value) {
        return (T) registry.put(key, value);
    }

    /**
     * Set the value of the given key if the key is already registered.
     * @param key   data key
     * @param value data value
     * @return previous data stored with this key
     */
    @Override
    public <T> @Nullable T setIfPresent(@NotNull String key, @NotNull T value) {
        return has(key) ? set(key, value) : null;
    }

    /**
     * Set the value of the given key if the key is not registered yet.
     * @param key   data key
     * @param value data value
     * @return previous data stored with this key
     */
    @Override
    public <T> @Nullable T setIfAbsent(@NotNull String key, @NotNull T value) {
        return !has(key) ? set(key, value) : null;
    }

    /**
     * Remove a data from the registry.
     * @param key data key
     * @return removed data value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T remove(@NotNull String key) {
        return (T) registry.remove(key);
    }

    /**
     * Clear all the registered values.
     */
    @Override
    public void clear() {
        registry.clear();
    }
}
