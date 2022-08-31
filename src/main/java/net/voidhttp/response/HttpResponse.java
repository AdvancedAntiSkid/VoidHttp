package net.voidhttp.response;

import com.google.gson.JsonObject;
import net.voidhttp.header.Headers;
import net.voidhttp.header.HttpHeaders;
import net.voidhttp.response.cookie.Cookies;
import net.voidhttp.response.cookie.ResponseCookies;
import net.voidhttp.util.asset.Asset;
import net.voidhttp.util.asset.MIMEType;
import net.voidhttp.util.Placeholder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Represents a HTTP server response to a client HTTP request.
 */
public class HttpResponse implements Response {
    /**
     * The requesting client socket.
     */
    private final Socket socket;

    /**
     * The registry of the response headers.
     */
    private final Headers headers;

    /**
     * The registry of the response cookies.
     */
    private final Cookies cookies;

    /**
     * The response status code.
     */
    private int code = 200;

    /**
     * The response status message
     */
    private String message = "";

    /**
     * Initialize the HTTP response.
     * @param socket client socket
     */
    public HttpResponse(Socket socket) {
        this.socket = socket;
        headers = HttpHeaders.empty();
        cookies = new ResponseCookies();
    }

    /**
     * Response to the request with raw bytes and a content type given.
     * @param bytes response bytes
     * @param type content type
     */
    @Override
    public void send(byte[] bytes, MIMEType type) throws IOException {
        // create data output writer
        OutputStream stream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        // write the response status
        writer.println("HTTP/1.1 " + code + " " + message);
        // write the default header values if they are missing
        headers.addIfAbsent("Server", "VoidHttp 1.0");
        headers.addIfAbsent("Date", new Date());
        headers.addIfAbsent("Content-type", type);
        headers.addIfAbsent("Content-length", bytes.length);
        // write the response headers
        cookies.write(headers);
        headers.write(writer);
        // write a blank line after the end of headers which
        // determines that the response body has begun
        writer.println();
        writer.flush();
        // write the body of the response
        stream.write(bytes);
        stream.flush();
        // close the connection
        stream.close();
    }

    /**
     * Response to the request with raw bytes.
     * @param bytes response bytes
     */
    @Override
    public void send(byte[] bytes) throws IOException {
        send(bytes, MIMEType.BINARY);
    }

    /**
     * Respond to the request with a raw text data and a content type given.
     * @param data response text
     * @param type content type
     */
    @Override
    public void send(String data, MIMEType type) throws IOException {
        send(data.getBytes(StandardCharsets.UTF_8), type);
    }

    /**
     * Respond to the request with a raw text data.
     * @param data response text
     */
    @Override
    public void send(String data) throws IOException {
        send(data, MIMEType.HTML);
    }

    /**
     * Respond to the request with a json data.
     * @param json response json
     */
    @Override
    public void send(JsonObject json) throws IOException {
        send(json.toString(), MIMEType.JSON);
    }

    /**
     * Respond to the request with a template.
     * @param template server template
     * @param placeholders template placeholders
     */
    @Override
    public void render(String template, boolean cache, Placeholder... placeholders) throws IOException {
        // get the template from cache
        String path = "./templates/" + template + ".html";
        String content = cache ? Asset.getUTF(path) : Asset.loadUTF(path);
        // replace the template placeholders
        for (Placeholder placeholder : placeholders) {
            content = content.replace(placeholder.getKey(), placeholder.getValue());
        }
        // send the built template
        send(content, MIMEType.HTML);
    }

    /**
     * Respond to the request with a template.
     * @param template server template
     * @param placeholders template placeholders
     */
    @Override
    public void render(String template, Placeholder... placeholders) throws IOException {
        render(template, false, placeholders);
    }

    /**
     * Redirect the client to the given url.
     * @param url redirect url
     * @param seconds seconds to wait before the redirection
     */
    @Override
    public void redirect(String url, int seconds) throws IOException {
        send(String.format("<meta http-equiv=\"Refresh\" content=\"%s; url='%s'\" />", seconds, url));
    }

    /**
     * Redirect the client to the given url.
     * @param url redirect url
     */
    @Override
    public void redirect(String url) throws IOException {
        redirect(url, 0);
    }

    /**
     * Set the response status code.
     * @param code new status code
     */
    @Override
    public Response status(int code) {
        this.code = code;
        return this;
    }

    /**
     * Set the response status message
     * @param message new status message
     */
    @Override
    public Response message(String message) {
        this.message = message;
        return this;
    }

    /**
     * Get the registry of the response headers.
     */
    @Override
    public Headers headers() {
        return headers;
    }

    /**
     * Get the registry of the response cookies.
     */
    @Override
    public Cookies cookies() {
        return cookies;
    }
}
