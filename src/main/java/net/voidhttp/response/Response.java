package net.voidhttp.response;

import com.google.gson.JsonObject;
import net.voidhttp.header.Headers;
import net.voidhttp.response.cookie.Cookies;
import net.voidhttp.util.asset.MIMEType;
import net.voidhttp.util.Placeholder;
import net.voidhttp.util.json.JsonBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Represents a HTTP server response to a client HTTP request.
 */
public interface Response {
    /**
     * Response to the request with raw bytes and a content type given.
     * @param bytes response bytes
     * @param type content type
     */
    void send(byte[] bytes, MIMEType type) throws IOException;

    /**
     * Response to the request with raw bytes.
     * @param bytes response bytes
     */
    void send(byte[] bytes) throws IOException;

    /**
     * Respond to the request with a raw text data and a content type given.
     * @param data response text
     * @param type content type
     */
    void send(String data, MIMEType type) throws IOException;

    /**
     * Respond to the request with a raw text data.
     * @param data response text
     */
    void send(String data) throws IOException;

    /**
     * Respond to the request with a json data.
     * @param json response json
     */
    void send(JsonObject json) throws IOException;

    /**
     * Respond to the request with a json data.
     * @param builder response json builder
     */
    void send(JsonBuilder builder) throws IOException;

    /**
     * Respond to the request with a file content.
     * @param file target file
     * @throws IOException error whilst sending
     */
    void sendFile(File file) throws IOException;

    /**
     * Respond to the request with a file content.
     * @param path target file path
     * @throws IOException error whilst sending
     */
    void sendFile(String path) throws IOException;

    /**
     * Respond to the request with a template.
     * @param template server template
     * @param placeholders template placeholders
     */
    void render(String template, boolean cache, Placeholder... placeholders) throws IOException;

    /**
     * Respond to the request with a template.
     * @param template server template
     * @param placeholders template placeholders
     */
    void render(String template, Placeholder... placeholders) throws IOException;

    /**
     * Redirect the client to the given url.
     * @param url redirect url
     * @param seconds seconds to wait before the redirection
     */
    void redirect(String url, int seconds) throws IOException;

    /**
     * Redirect the client to the given url.
     * @param url redirect url
     */
    void redirect(String url) throws IOException;

    /**
     * Set the response status code.
     * @param code new status code
     */
    Response status(int code);

    /**
     * Set the response status message
     * @param message new status message
     */
    Response message(String message);

    /**
     * Get the registry of the response headers.
     */
    Headers headers();

    /**
     * Get the registry of the response cookies.
     */
    Cookies cookies();
}
