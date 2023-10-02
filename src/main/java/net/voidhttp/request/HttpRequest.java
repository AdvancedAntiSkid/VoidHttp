package net.voidhttp.request;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.voidhttp.header.Header;
import net.voidhttp.header.Headers;
import net.voidhttp.header.HttpHeaders;
import net.voidhttp.request.cookie.Cookies;
import net.voidhttp.request.cookie.RequestCookies;
import net.voidhttp.request.data.Data;
import net.voidhttp.request.data.RequestData;
import net.voidhttp.request.parameter.Parameters;
import net.voidhttp.request.parameter.RequestParameters;
import net.voidhttp.request.query.Query;
import net.voidhttp.request.query.RequestQuery;
import net.voidhttp.request.session.Session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;

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
     * The action to be invoked when the request is handled.
     */
    private BiConsumer<Method, String> action;

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
     * The current session of the request.
     */
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
    private void handle() {
        try {
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
            this.method = Method.of(tokenizer.nextToken().toUpperCase());
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
            // get the length of the body
            // read the body of the request
            String contentLength = headers.get("Content-Length");
            if (contentLength != null) {
                StringBuilder builder = new StringBuilder();
                int length = Integer.parseInt(contentLength);
                // read the remaining parts of the request content
                for (int i = 0; i < length; i++)
                    builder.append((char) reader.read());
                body = builder.toString();
            }
            // get the type of the requested content
            String contentType = headers.get("content-type");
            if (contentType != null && contentType.startsWith("application/json")) {
                // parse the request body to json
                try {
                    json = (JsonObject) JsonParser.parseString(body);
                } catch (Exception ignored) {}
            }
            // request has been processed
            // call the request done handlers
            action.accept(method, route);
        }
        // unable to process request
        catch (Exception e) {
            // TODO send the stack trace
            e.printStackTrace();
        }
    }

    /**
     * Open the HTTP request and start request handling.
     * @param action request complete handler
     */
    public void open(BiConsumer<Method, String> action) {
        this.action = action;
        handle();
    }

    /**
     * Get the requested url.
     */
    @Override
    public String route() {
        return route;
    }

    /**
     * Get the HTTP request method used.
     */
    @Override
    public Method method() {
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
    public InetAddress host() {
        return host;
    }

    /**
     * Get the registry of the requested headers.
     */
    @Override
    public Headers headers() {
        return headers;
    }

    /**
     * Get the registry of request cookies.
     */
    @Override
    public Cookies cookies() {
        return cookies;
    }

    /**
     * Get the registry of the passed values.
     */
    @Override
    public Data data() {
        return data;
    }

    /**
     * Get the request body content.
     */
    @Override
    public String body() {
        return body;
    }

    /**
     * Get the request body json.
     */
    @Override
    public JsonObject json() {
        return json;
    }

    /**
     * Get the request parameters.
     */
    @Override
    public Parameters parameters() {
        return parameters;
    }

    /**
     * Get the current request session.
     */
    @Override
    public Session session() {
        return session;
    }

    /**
     * The query data of the url.
     */
    @Override
    public Query query() {
        return query;
    }

    /**
     * Set the query of the request.
     * @param query new query
     */
    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * Set the current request session.
     */
    @Override
    public void setSession(Session session) {
        this.session = session;
    }

}
