package net.voidhttp.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class JsonArrayBuilder {
    private final JsonArray json;

    public JsonArrayBuilder() {
        json = new JsonArray();
    }

    public JsonArrayBuilder push(Number value) {
        json.add(value);
        return this;
    }

    public JsonArrayBuilder push(String value) {
        json.add(value);
        return this;
    }

    public JsonArrayBuilder push(Boolean value) {
        json.add(value);
        return this;
    }

    public JsonArrayBuilder push(Character character) {
        json.add(character);
        return this;
    }

    public JsonArrayBuilder push(JsonArray value) {
        json.add(value);
        return this;
    }

    public JsonArrayBuilder push(JsonBuilder value) {
        json.add(value.build());
        return this;
    }

    public JsonArrayBuilder push(JsonArrayBuilder value) {
        json.add(value.build());
        return this;
    }

    public JsonArrayBuilder push(Object value) {
        json.add(value != null ? new JsonPrimitive(String.valueOf(value)) : JsonNull.INSTANCE);
        return this;
    }

    public JsonArray build() {
        return json;
    }
}
