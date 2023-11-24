package net.voidhttp.request;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.voidhttp.header.Headers;
import net.voidhttp.header.HttpHeaders;
import net.voidhttp.request.cookie.Cookies;
import net.voidhttp.request.cookie.RequestCookies;
import net.voidhttp.request.data.Data;
import net.voidhttp.request.data.RequestData;
import net.voidhttp.request.form.FormEntry;
import net.voidhttp.request.form.MultipartForm;
import net.voidhttp.request.form.RequestFormEntry;
import net.voidhttp.request.form.RequestMultipartForm;
import net.voidhttp.request.parameter.Parameters;
import net.voidhttp.request.parameter.RequestParameters;
import net.voidhttp.request.query.Query;
import net.voidhttp.request.query.RequestQuery;
import net.voidhttp.request.session.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Represents a client http request.
 */
public class HttpRequest implements Request {
    /**
     * The connecting client socket.
     */
    @Getter
    private final Socket socket;

    /**
     * The requesting client's address.
     */
    private final InetAddress host;

    /**
     * The query of the request url.
     */
    private Query query;

    /**
     * The request method.
     */
    private Method method;

    /**
     * The requested url.
     */
    private String route;

    /**
     * The request url parameters.
     */
    private Parameters parameters;

    /**
     * The registry of the request headers.
     */
    private Headers headers;


    /**
     * The registry of the request cookies.
     */
    private Cookies cookies;

    /**
     * The registry of the passed data.
     */
    private Data data;

    /**
     * The body of the request.
     */
    private String body;

    /**
     * The json body of the request.
     */
    private JsonObject json;

    /**
     * Get the parsed multipart/form-data body of the request.
     */
    private MultipartForm multipart;

    /**
     * The current session of the request.
     */
    @Setter
    private Session session;

    /**
     * Determines if the current request was passed.
     */
    private boolean passed;

    /**
     * Initialize the http request.
     * @param socket connecting socket
     */
    public HttpRequest(Socket socket) {
        this.socket = socket;
        host = socket.getInetAddress();
        query = new RequestQuery(new HashMap<>());
    }

    /**
     * Handle the http request.
     */
    public void open() throws Exception {
        // read characters from the client via input stream on the socket
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // create a tokenizer for parsing input
        StringTokenizer tokenizer;
        try {
            tokenizer = new StringTokenizer(reader.readLine());
        } catch (NullPointerException e) {
            return;
        }

        // determine the request method
        String methodToken = tokenizer.nextToken().toUpperCase();
        method = Method.of(methodToken);
        if (method == null)
            throw new RuntimeException("Invalid request method: " + methodToken);

        // get the requested url
        // the route and parameters are separated using a question mark
        String[] url = tokenizer.nextToken().split("\\?");
        route = url[0];
        // parse the url parameters
        parameters = url.length > 1
            ? RequestParameters.parse(url[1])
            : RequestParameters.empty();

        // read the headers of the request
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            // the headers and the request body is separated using an empty line
            // stop processing headers if the line is empty
            if (line.isEmpty())
                break;
            // append the header line
            lines.add(line);
        }

        // parse the request headers
        headers = HttpHeaders.parse(lines);
        // parse the request cookies
        // check if there is a header with the key "cookie"
        String header = headers.get("cookie");
        cookies = header != null
            ? RequestCookies.parse(header)
            : RequestCookies.empty();
        // create request transfer data holder
        data = new RequestData();

        // get the type of the data sent
        String contentType = headers.get("content-type");

        // handle multipart/form-data request body parsing
        // sadly in this case, the content length is not guaranteed, therefore we have to separately parse the body
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            // get the boundary of the multipart/form-data body that will indicate
            // how the form entries should be separated and when the body ends
            String boundary = contentType.split(";")[1].split("=")[1];

            // parse the multipart/form-data body
            parseMultipartData(reader, boundary);
            return;
        }

        // TODO: parse application/x-www-form-urlencoded
        // else if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
        //     return;
        // }

        // get the length of the body
        // read the body of the request
        String contentLength = headers.get("Content-Length");
        if (contentLength != null) {
            int length = Integer.parseInt(contentLength);
            System.out.println("length: " + length);

            StringBuilder builder = new StringBuilder();
            // read the remaining parts of the request content
            for (int i = 0; i < length; i++) {
                char read = (char) reader.read();
                builder.append(read);
                System.out.print(read);
            }
            body = builder.toString();
        }

        // get the type of the requested content
        if (contentType != null && contentType.startsWith("application/json")) {
            // parse the request body to json
            try {
                json = (JsonObject) JsonParser.parseString(body);
            } catch (Exception ignored) {}
        }

        System.out.println("\n\nrequest done");
    }

    /**
     * Parse the multipart/form-data body of the request. Unfortunately, multipart/form-data does not guarantee
     * a proper content length, so we have to read lines until we find the boundary and a <code>--</code> suffix.
     * @param reader reader of the request body
     * @param boundary boundary of the multipart/form-data body
     */
    @SneakyThrows
    private void parseMultipartData(BufferedReader reader, String boundary) {
        boolean started = false;

        List<String> lines = new ArrayList<>();
        List<FormEntry> entries = new ArrayList<>();

        // read all the available lines from the connecting socket
        String line;
        while ((line = reader.readLine()) != null) {
            // check if a new form entry has started
            if (line.equals("--" + boundary)) {
                // indicate, that the first form entry has started
                if (!started)
                    started = true;
                // if the form entries have already started, parse the previous one
                else {
                    entries.add(RequestFormEntry.parse(lines));
                    lines = new ArrayList<>();
                }
            }

            // check if the form entries have ended
            else if (line.equals("--" + boundary + "--")) {
                // parse the last form entry and stop reading
                entries.add(RequestFormEntry.parse(lines));
                break;
            }

            // if the form entries have started, add the line to the current form entry
            else
                lines.add(line);
        }

        multipart = new RequestMultipartForm(entries);
    }

    /**
     * Get the requested url.
     */
    @Override
    public @NotNull String route() {
        return route;
    }

    /**
     * Get the HTTP request method used.
     */
    @Override
    public @NotNull Method method() {
        return method;
    }

    /**
     * The current handler has passed executing.
     * Mark the request as done.
     */
    @Override
    public void next() {
        passed = true;
    }

    /**
     * Reset the request pass state.
     */
    @Override
    public void reset() {
        passed = false;
    }

    /**
     * Determine if the handler has passed executing.
     */
    @Override
    public boolean passed() {
        return passed;
    }

    /**
     * Get the address of the requesting client.
     * @return ip address
     */
    @Override
    public @NotNull InetAddress host() {
        return host;
    }

    /**
     * Get the registry of the requested headers.
     */
    @Override
    public @NotNull Headers headers() {
        return headers;
    }

    /**
     * Get the registry of request cookies.
     */
    @Override
    public @NotNull Cookies cookies() {
        return cookies;
    }

    /**
     * Get the registry of the passed values.
     */
    @Override
    public @NotNull Data data() {
        return data;
    }

    /**
     * Get the request body content.
     */
    @Override
    public @Nullable String body() {
        return body;
    }

    /**
     * Get the request body json.
     */
    @Override
    public @Nullable JsonObject json() {
        return json;
    }

    /**
     * Get the parsed multipart/form-data body of the request.
     */
    @Override
    public MultipartForm multipart() {
        return multipart;
    }

    /**
     * Get the request parameters.
     */
    @Override
    public @NotNull Parameters parameters() {
        return parameters;
    }

    /**
     * Get the current request session.
     */
    @Override
    public @Nullable Session session() {
        return session;
    }

    /**
     * The query data of the url.
     */
    @Override
    public @NotNull Query query() {
        return query;
    }

    /**
     * Set the query of the request.
     * @param query new query
     */
    public void setQuery(Query query) {
        this.query = query;
    }
}
