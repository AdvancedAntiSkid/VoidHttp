package net.voidhttp.request.data;

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
    public boolean has(String key) {
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
    public <T> T get(String key, T defaultValue) {
        return (T) registry.getOrDefault(key, defaultValue);
    }

    /**
     * Get a T value from the data registry.
     * @param key data key
     * @return data value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
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
    public <T> T get(String key, Class<T> type, T defaultValue) {
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
    public <T> T get(String key, Class<T> type) {
        return get(key, type, null);
    }

    /**
     * Get an object value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public Object getObject(String key, Object defaultValue) {
        return registry.getOrDefault(key, defaultValue);
    }

    /**
     * Get an object value from the data registry.
     * @param key data key
     * @return data value
     */
    @Override
    public Object getObject(String key) {
        return getObject(key, null);
    }

    /**
     * Get a string value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public String getString(String key, String defaultValue) {
        return (String) registry.getOrDefault(key, defaultValue);
    }

    /**
     * Get a string value from the data registry.
     * @param key data key
     * @return data value
     */
    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * Get a boolean value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
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
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Get an int value from the data registry.
     * @param key          data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public int getInt(String key, int defaultValue) {
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
    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * Get a long value from the data registry.
     * @param key          data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public long getLong(String key, long defaultValue) {
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
    public long getLong(String key) {
        return getLong(key, 0L);
    }

    /**
     * Get a float value from the data registry.
     * @param key          data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public float getFloat(String key, float defaultValue) {
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
    public float getFloat(String key) {
        return getFloat(key, 0F);
    }

    /**
     * Get a double value from the data registry.
     * @param key          data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    @Override
    public double getDouble(String key, double defaultValue) {
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
    public double getDouble(String key) {
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
    public <T> T set(String key, T value) {
        return (T) registry.put(key, value);
    }

    /**
     * Set the value of the given key if the key is already registered.
     * @param key   data key
     * @param value data value
     * @return previous data stored with this key
     */
    @Override
    public <T> T setIfPresent(String key, T value) {
        return has(key) ? set(key, value) : null;
    }

    /**
     * Set the value of the given key if the key is not registered yet.
     * @param key   data key
     * @param value data value
     * @return previous data stored with this key
     */
    @Override
    public <T> T setIfAbsent(String key, T value) {
        return !has(key) ? set(key, value) : null;
    }

    /**
     * Remove a data from the registry.
     * @param key data key
     * @return removed data value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T remove(String key) {
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
