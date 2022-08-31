package net.voidhttp.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Represents a utility that merges json objects and handles merge conflicts.
 */
public class JsonMerger {
    /**
     * Marge two json objects into each other using a conflict strategy.
     * @param source source object to merge into
     * @param update update object to merge from
     * @param strategy conflict strategy to use
     */
    public static void merge(JsonObject source, JsonObject update, ConflictStrategy strategy) {
        // loop through the update pairs
        for (Map.Entry<String, JsonElement> entry : update.entrySet()) {
            // get the key and value of the pair
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            // check if the key is already exists in the source
            if (source.has(key)) {
                // get the value of the key from the source
                JsonElement sourceValue = source.get(key);
                // check if the source and the update are arrays
                if (sourceValue.isJsonArray() && value.isJsonArray()) {
                    // get the arrays as values
                    JsonArray sourceArray = sourceValue.getAsJsonArray();
                    JsonArray updateArray = value.getAsJsonArray();
                    // concat the arrays, nothing to override
                    for (JsonElement element : updateArray) {
                        sourceArray.add(element);
                    }
                }
                // check if the source and the update are objects
                else if (sourceValue.isJsonObject() && value.isJsonObject()) {
                    // marge the json objects recursively
                    merge(sourceValue.getAsJsonObject(), value.getAsJsonObject(), strategy);
                }
                // types does not match or are recursive
                else {
                    // handle merge conflict
                    switch (strategy) {
                        // don't modify anything, keep the original value
                        case KEEP_ORIGINAL:
                            break;
                        // force override the original value
                        case OVERRIDE:
                            source.add(key, value);
                            break;
                        // override the original value if the update value is not null
                        case OVERRIDE_NOT_NULL:
                            if (!value.isJsonNull()) {
                                source.add(key, value);
                            }
                            break;
                        // throw an exception if the update key is already exists in the source
                        case THROW_EXCEPTION:
                            throw new IllegalStateException("Key '" + key + "' already exists in object " + source);
                    }
                }
            }
            // key does not exists, add the value
            else {
                source.add(key, value);
            }
        }
    }
}
