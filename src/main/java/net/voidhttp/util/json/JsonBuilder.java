package net.voidhttp.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents a JsonObject building utility.
 */
public class JsonBuilder {
    private final JsonObject json = new JsonObject();

    public JsonBuilder() {
    }

    public JsonBuilder(String key, Number value) {
        json.addProperty(key, value);
    }

    public JsonBuilder(String key, String value) {
        json.addProperty(key, value);
    }

    public JsonBuilder(String key, Boolean value) {
        json.addProperty(key, value);
    }

    public JsonBuilder(String key, Character value) {
        json.addProperty(key, value);
    }

    public JsonBuilder(String key, JsonBuilder value) {
        json.add(key, value.build());
    }

    public JsonBuilder(String key, JsonElement value) {
        json.add(key, value);
    }

    public JsonBuilder(String key, Object value) {
        json.addProperty(key, value != null ? String.valueOf(value) : null);
    }

    public JsonBuilder set(String key, Number value) {
        json.addProperty(key, value);
        return this;
    }

    public JsonBuilder set(String key, String value) {
        json.addProperty(key, value);
        return this;
    }

    public JsonBuilder set(String key, Boolean value) {
        json.addProperty(key, value);
        return this;
    }

    public JsonBuilder set(String key, Character value) {
        json.addProperty(key, value);
        return this;
    }

    public JsonBuilder set(String key, JsonBuilder value) {
        json.add(key, value.build());
        return this;
    }

    public JsonBuilder set(String key, JsonArrayBuilder value) {
        json.add(key, value.build());
        return this;
    }

    public JsonBuilder set(String key, JsonElement value) {
        json.add(key, value);
        return this;
    }

    public JsonBuilder set(String key, Object value) {
        json.addProperty(key, value != null ? String.valueOf(value) : null);
        return this;
    }

    public JsonBuilder merge(JsonObject update, ConflictStrategy strategy) {
        JsonMerger.merge(json, update, strategy);
        return this;
    }

    public JsonBuilder merge(JsonObject update) {
        return merge(update, ConflictStrategy.OVERRIDE);
    }

    public JsonBuilder merge(JsonBuilder update, ConflictStrategy strategy) {
        return merge(update.build(), strategy);
    }

    public JsonBuilder merge(JsonBuilder update) {
        return merge(update, ConflictStrategy.OVERRIDE);
    }

    public JsonObject build() {
        return json;
    }
}
