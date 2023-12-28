package net.voidhttp.response;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.voidhttp.HttpServer;
import net.voidhttp.config.Flag;
import net.voidhttp.controller.dto.Dto;
import net.voidhttp.header.Headers;
import net.voidhttp.header.HttpHeaders;
import net.voidhttp.response.cookie.Cookies;
import net.voidhttp.response.cookie.ResponseCookies;
import net.voidhttp.util.asset.Asset;
import net.voidhttp.util.asset.MIMEType;
import net.voidhttp.util.Placeholder;
import net.voidhttp.util.json.JsonBuilder;

import java.io.*;
import java.net.Socket;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Represents an HTTP server response to a client HTTP request.
 */
public class HttpResponse implements Response {
    /**
     * The json serializer and deserializer.
     */
    private static final Gson gson = new Gson();

    /**
     * The server that handles the http response.
     */
    @Getter
    private final HttpServer server;

    /**
     * The requesting client socket channel.
     */
    private final AsynchronousSocketChannel channel;

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
     * @param server the server that handles the http response
     * @param channel the requesting client socket channel
     */
    public HttpResponse(HttpServer server, AsynchronousSocketChannel channel) {
        this.server = server;
        this.channel = channel;

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
        if (2 == 2)
            return;

        // create data output writer
        OutputStream stream = null; //  socket.getOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        // write the response status
        writer.println("HTTP/1.1 " + code + " " + message);
        // write the default header values if they are missing
        if (!server.getConfig().hasFlag(Flag.NO_SERVER_NAME))
            headers.addIfAbsent("Server", "VoidHttp 1.0");
        headers.addIfAbsent("Date", currentDateTime());
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
     * Get the current date according to the HTTP date format.
     * @return current date time RFC 1123 representation
     */
    private String currentDateTime() {
        // Create a Date object representing the current date and time
        Date currentDate = new Date();
        // Create a SimpleDateFormat with the desired format and locale
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        // Set the time zone to GMT (Greenwich Mean Time)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Format the date as a string
        return dateFormat.format(currentDate);
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
     * Respond to the request with a json data.
     * @param builder response json builder
     */
    @Override
    public void send(JsonBuilder builder) throws IOException {
        send(builder.build().toString(), MIMEType.JSON);
    }

    /**
     * Respond to the request with a file content.
     * @param file target file
     * @throws IOException error whilst sending
     */
    @Override
    public void sendFile(File file) throws IOException {
        // get the input stream of the asset file
        try (InputStream stream = Files.newInputStream(file.toPath())) {
            // load the content of the file
            byte[] bytes = ByteStreams.toByteArray(stream);
            // get the extension of the file
            String name = file.getName();
            String extension = name.substring(name.lastIndexOf('.'));
            // send the file content to the client
            send(bytes, MIMEType.fromExtensionOrDefault(extension, MIMEType.PLAIN_TEXT));
        }
    }

    /**
     * Respond to the request with a file content.
     * @param path target file path
     * @throws IOException error whilst sending
     */
    @Override
    public void sendFile(String path) throws IOException {
        sendFile(new File(path));
    }

    /**
     * Respond to the request with an error.
     * @param error target error
     * @throws IOException error whilst sending
     */
    @Override
    public void sendError(Throwable error) throws IOException {
        // capture the stack trace to a string writer
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        error.printStackTrace(printer);
        // send the error log wrapped with a <pre> tag
        status(400).send("<pre>" + writer.toString() +  "</pre>");
    }

    /**
     * Respond to the request with a data transfer object.
     * @param object response object
     * @param <T> object type
     * @throws IOException error whilst sending
     */
    @Override
    public <T> void sendObject(T object) throws IOException {
        // check if the object is not a transfer object
        if (!object.getClass().isAnnotationPresent(Dto.class))
            throw new IllegalArgumentException(
                "Cannot send non-transfer-object as response for security reasons. " +
                "If you are sure this is safe, annotate the class with @Dto."
            );
        // serialize the object to json
        send(gson.toJson(object), MIMEType.JSON);
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
        for (Placeholder placeholder : placeholders)
            content = content.replace(placeholder.getKey(), placeholder.getValue());
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
