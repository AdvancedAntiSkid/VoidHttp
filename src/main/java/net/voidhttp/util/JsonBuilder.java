package net.voidhttp.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents a JsonObject building utility.
 */
public class JsonBuilder {
    private final JsonObject json;

    public JsonBuilder() {
        json = new JsonObject();
    }

    public JsonBuilder(String key, Number value) {
        json = new JsonObject();
        json.addProperty(key, value);
    }

    public JsonBuilder(String key, String value) {
        json = new JsonObject();
        json.addProperty(key, value);
    }

    public JsonBuilder(String key, Boolean value) {
        json = new JsonObject();
        json.addProperty(key, value);
    }

    public JsonBuilder(String key, Character value) {
        json = new JsonObject();
        json.addProperty(key, value);
    }

    public JsonBuilder(String key, JsonBuilder value) {
        json = new JsonObject();
        json.add(key, value.build());
    }

    public JsonBuilder(String key, JsonElement value) {
        json = new JsonObject();
        json.add(key, value);
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

    public JsonBuilder set(String key, JsonElement value) {
        json.add(key, value);
        return this;
    }

    public JsonObject build() {
        return json;
    }
}
