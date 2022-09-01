package net.voidhttp.request.data;

/**
 * Represents a request data manager that can be used to pass data between request handlers.
 */
public interface Data {
    /**
     * Determine if the given key exists.
     * @param key data key
     * @return data exists
     */
    boolean has(String key);

    /**
     * Get a T value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @param <T> data type
     * @return data value
     */
    <T> T get(String key, T defaultValue);

    /**
     * Get a T value from the data registry.
     * @param key data key
     * @param <T> data type
     * @return data value
     */
    <T> T get(String key);

    /**
     * Get an object value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    Object getObject(String key, Object defaultValue);

    /**
     * Get an object value from the data registry.
     * @param key data key
     * @return data value
     */
    Object getObject(String key);

    /**
     * Get a string value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    String getString(String key, String defaultValue);

    /**
     * Get a string value from the data registry.
     * @param key data key
     * @return data value
     */
    String getString(String key);

    /**
     * Get a boolean value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Get a boolean value from the data registry.
     * @param key data key
     * @return data value
     */
    boolean getBoolean(String key);

    /**
     * Get an int value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    int getInt(String key, int defaultValue);

    /**
     * Get an int value from the data registry.
     * @param key data key
     * @return data value
     */
    int getInt(String key);

    /**
     * Get a long value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    long getLong(String key, long defaultValue);

    /**
     * Get a long value from the data registry.
     * @param key data key
     * @return data value
     */
    long getLong(String key);

    /**
     * Get a float value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    float getFloat(String key, float defaultValue);

    /**
     * Get a float value from the data registry.
     * @param key data key
     * @return data value
     */
    float getFloat(String key);

    /**
     * Get a double value from the data registry.
     * @param key data key
     * @param defaultValue value to return if the key is unregistered
     * @return data value
     */
    double getDouble(String key, double defaultValue);

    /**
     * Get a double value from the data registry.
     * @param key data key
     * @return data value
     */
    double getDouble(String key);

    /**
     * Set the value of the given key.
     * @param key data key
     * @param value data value
     * @param <T> data type
     * @return previous data stored with this key
     */
    <T> T set(String key, T value);

    /**
     * Set the value of the given key if the key is already registered.
     * @param key data key
     * @param value data value
     * @param <T> data type
     * @return previous data stored with this key
     */
    <T> T setIfPresent(String key, T value);

    /**
     * Set the value of the given key if the key is not registered yet.
     * @param key data key
     * @param value data value
     * @param <T> data type
     * @return previous data stored with this key
     */
    <T> T setIfAbsent(String key, T value);

    /**
     * Remove a data from the registry.
     * @param key data key
     * @param <T> data type
     * @return removed data value
     */
    <T> T remove(String key);

    /**
     * Clear all the registered values.
     */
    void clear();
}
