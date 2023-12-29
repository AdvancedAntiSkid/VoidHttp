package net.voidhttp.request;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.inventex.octa.concurrent.future.Future;
import dev.inventex.octa.data.primitive.Tuple;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.voidhttp.ServerConfig;
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
import net.voidhttp.response.PushbackBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * Represents a client http request.
 */
public class HttpRequest implements Request {
    /**
     * The connecting client socket channel.
     */
    @Getter
    private final AsynchronousSocketChannel channel;

    /**
     * The requesting client's address.
     */
    private final InetAddress host;

    /**
     * The configuration of the http server.
     */
    private final ServerConfig config;

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
     * The binary body of the request.
     */
    private byte[] binary;

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

    private final Future<Void> completionHandler = new Future<>();

    private final List<String> headerLines = new ArrayList<>();
    private String lastHeaderLinePart;
    private boolean incompleteHeader;

    private int totalHeaderSize;

    private final ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();
    private int contentLength;

    /**
     * Initialize the http request.
     * @param channel the connecting client socket channel
     * @param config the configuration of the server
     */
    @SneakyThrows
    public HttpRequest(AsynchronousSocketChannel channel, ServerConfig config) {
        this.channel = channel;
        this.config = config;

        SocketAddress remoteAddress = channel.getRemoteAddress();
        if (remoteAddress instanceof InetSocketAddress inet)
            host = inet.getAddress();
        else
            throw new IOException("Unrecognized socket address: " + remoteAddress);

        query = new RequestQuery(new HashMap<>());
    }

    public Future<Void> parse() {
        nextChunk(ReadState.HEADERS_START);
        return completionHandler;
    }

    private void nextChunk(ReadState nextState) {
        switch (nextState) {
            case HEADERS_START -> handleHeaderStart();
            case HEADERS_CONTINUE -> handleHeaderContinue();
            case HEADERS_PARSE -> handleHeaderParse();
            case SIZED_CONTENT_START -> handleSizedContentStart();
            case SIZED_CONTENT_CONTINUE -> handleSizedContentContinue();
            case SIZED_CONTENT_PARSE -> handleSizedContentParse();
            default -> throw new RuntimeException("Unhandled end of handler chain: " + nextState);
        };
    }

    private void handleHeaderStart() {
        System.err.println("START HEADERS");
        readChannel(config.getHeaderReadSize()).tryThen(buffer -> {
            String descriptor = buffer.readLine();
            if (descriptor == null)
                return;

            StringTokenizer tokenizer = new StringTokenizer(descriptor);

            // determine the request method
            String methodToken = tokenizer.nextToken().toUpperCase();
            method = Method.of(methodToken);
            if (method == null)
                throw new RuntimeException("Invalid request method: " + methodToken);

            System.err.println("METHOD: " + methodToken);

            // get the requested url
            // the route and parameters are separated using a question mark
            String[] url = tokenizer.nextToken().split("\\?");
            route = url[0];
            // parse the url parameters
            parameters = url.length > 1
                ? RequestParameters.parse(url[1])
                : RequestParameters.empty();

            // read the headers of the request
            Tuple<String, Boolean> line;
            while ((line = buffer.readHeaderLine()) != null) {
                String header = line.getFirst();
                boolean newLine = line.getSecond();
                lastHeaderLinePart = header;

                // check if the header size exceeded the initial 4kb buffer
                // this means a header is incomplete, and we have to read the next chunk
                if (!newLine) {
                    incompleteHeader = true;
                    nextChunk(ReadState.HEADERS_CONTINUE);
                    return;
                }

                // the headers and the request body is separated using an empty line
                // stop processing headers if the line is empty
                if (header.isEmpty()) {
                    // append the read amount of bytes to the total header size
                    totalHeaderSize += buffer.position();

                    // read all remaining bytes from the buffer
                    contentBuffer.write(buffer.readAllBytes());

                    nextChunk(ReadState.HEADERS_PARSE);
                    return;
                }

                // append the header line
                headerLines.add(header);
            }

            // headers are not finished yet, append the whole size of the current buffer
            totalHeaderSize += buffer.size();

            // end of buffer reached, but the header is not complete
            // this means we have to read the next chunk
            // no header line is incomplete, so we don't have to append the last header line part
            nextChunk(ReadState.HEADERS_CONTINUE);
        });
    }

    private void handleHeaderContinue() {
        System.err.println("CONTINUE HEADERS");
        // before reading the next header chunk, check if the header size has not exceeded the maximum size
        if (totalHeaderSize > config.getMaxHeaderSize())
            throw new RuntimeException(
                "Header size " + totalHeaderSize + " exceeded maximum size of " + config.getMaxHeaderSize() + " bytes"
            );

        readChannel(config.getHeaderReadSize()).tryThen(buffer -> {
            // check if a header was started in the previous chunk read
            if (incompleteHeader) {
                Tuple<String, Boolean> remainingLine = buffer.readHeaderLine();
                // legitimately, this should never happen
                if (remainingLine == null)
                    throw new RuntimeException("Unable to read remaining header line from");

                // legitimately, the header should not fill the entire chunk
                boolean newLine = remainingLine.getSecond();
                if (!newLine)
                    throw new RuntimeException("Header line exceeded chunk size");

                // register the remaining header line
                headerLines.add(lastHeaderLinePart + remainingLine.getFirst());
                incompleteHeader = false;
            }

            // read the headers of the request
            Tuple<String, Boolean> line;
            while ((line = buffer.readHeaderLine()) != null) {
                String header = line.getFirst();
                boolean newLine = line.getSecond();
                lastHeaderLinePart = header;

                // check if the header size exceeded the initial 4kb buffer
                // this means a header is incomplete, and we have to read the next chunk
                if (!newLine) {
                    incompleteHeader = true;
                    nextChunk(ReadState.HEADERS_CONTINUE);
                    return;
                }

                // the headers and the request body is separated using an empty line
                // stop processing headers if the line is empty
                if (header.isEmpty()) {
                    // append the read amount of bytes to the total header size
                    totalHeaderSize += buffer.position();

                    // read all remaining bytes from the buffer
                    contentBuffer.write(buffer.readAllBytes());

                    nextChunk(ReadState.HEADERS_PARSE);
                    return;
                }

                // append the header line
                headerLines.add(header);
            }

            // headers are not finished yet, append the whole size of the current buffer
            totalHeaderSize += buffer.size();

            // end of buffer reached, but the header is not complete
            // this means we have to read the next chunk
            // no header line is incomplete, so we don't have to append the last header line part
            nextChunk(ReadState.HEADERS_CONTINUE);
        });
    }

    private void handleHeaderParse() {
        System.err.println("PARSE HEADERS");

        // header processing has been finished, parse the headers
        headers = HttpHeaders.parse(headerLines);

        // parse the request cookies
        // check if there is a header with the key "cookie"
        String header = headers.get("cookie");
        cookies = header != null
            ? RequestCookies.parse(header)
            : RequestCookies.empty();

        // create request transfer data holder
        data = new RequestData();

        if (headers.has("content-type") && headers.get("content-type").equals("multipart/form-data"))
            throw new RuntimeException("multipart/form-data not implemented yet");

        handleSizedContentStart();
    }

    private void handleSizedContentStart() {
        System.err.println("START SIZED CONTENT");

        if (!headers.has("content-length"))
            throw new IllegalStateException(
                "Header `content-length` must be specified for `" + headers.get("content-type") + "` request"
            );

        contentLength = Integer.parseInt(headers.get("content-length"));

        handleSizedContentContinue();
    }

    private void handleSizedContentContinue() {
        System.err.println("CONTINUE SIZED CONTENT");

        int remainingBytes = contentLength - contentBuffer.size();
        int readSize = Math.min(config.getContentReadSize(), remainingBytes);
        readChannel(readSize).tryThen(buffer -> {
            contentBuffer.write(buffer.readAllBytes());

            if (contentBuffer.size() > config.getMaxContentLength())
                throw new RuntimeException(
                    "Content length " + contentBuffer.size() + " exceeded maximum size of " +
                    config.getMaxContentLength() + " bytes"
                );

            if (contentBuffer.size() < contentLength)
                handleSizedContentContinue();
            else
                handleSizedContentParse();
        });
    }

    private void handleSizedContentParse() {
        System.err.println("PARSE SIZED CONTENT");

        System.out.println("CONTENT: " + contentBuffer.toString(StandardCharsets.UTF_8));

        binary = contentBuffer.toByteArray();
        body = contentBuffer.toString(StandardCharsets.UTF_8);

        // get the type of the requested content
        if (headers.get("content-type").equals("application/json")) {
            // parse the request body to json
            try {
                json = (JsonObject) JsonParser.parseString(body);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Handle the http request.
     */
    private void open(PushbackBuffer stream, int firstBytesRead) throws Exception {
        // create a tokenizer for parsing input
        String descriptor = stream.readLine();
        if (descriptor == null)
            return;

        StringTokenizer tokenizer = new StringTokenizer(descriptor);

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
        while ((line = stream.readLine()) != null) {
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
            parseMultipartData(stream, boundary);
            return;
        }

        // TODO: parse application/x-www-form-urlencoded
        // else if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
        //     return;
        // }

        // read the body of the request as binary
        String contentLength = headers.get("Content-Length");
        if (contentLength != null) {
            int totalContentBytes = Integer.parseInt(contentLength);

            ByteArrayOutputStream data = new ByteArrayOutputStream(totalContentBytes);

            int contentStart = stream.position();
            int firstContentBytes = firstBytesRead - contentStart;

            byte[] buffer = new byte[config.getContentReadSize()];
            int bytesRead = stream.read(buffer, 0, firstContentBytes);
            data.write(buffer, 0, bytesRead);

            System.err.println("CONTENT: " + data.toString(StandardCharsets.UTF_8));

/*



            byte[] buffer = new byte[config.getChunkSize()];
            ByteArrayOutputStream data = new ByteArrayOutputStream(totalContentBytes);

            int bytesRead;
            int totalBytesRead = 0;

            while (
                totalBytesRead < totalContentBytes &&
                (bytesRead = stream.read(buffer, 0, Math.min(config.getChunkSize(), totalContentBytes - totalBytesRead))) != -1
            ) {
                data.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            binary = data.toByteArray();
            body = data.toString(StandardCharsets.UTF_8);

            if (totalBytesRead != totalContentBytes)
                Logger.warn("Invalid content totalContentBytes, read: " + totalBytesRead + ", expected: " + totalContentBytes);

 */
        }

        // get the type of the requested content
        if (contentType != null && contentType.startsWith("application/json")) {
            // parse the request body to json
            try {
                json = (JsonObject) JsonParser.parseString(body);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Parse the multipart/form-data body of the request. Unfortunately, multipart/form-data does not guarantee
     * a proper content length, so we have to read lines until we find the boundary and a <code>--</code> suffix.
     * @param stream reader of the request body
     * @param boundary boundary of the multipart/form-data body
     */
    @SneakyThrows
    private void parseMultipartData(PushbackBuffer stream, String boundary) {
        boolean started = false;

        List<String> lines = new ArrayList<>();
        List<FormEntry> entries = new ArrayList<>();

        // read all the available lines from the connecting socket
        String line;
        while ((line = stream.readLine()) != null) {
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

    private Future<PushbackBuffer> readChannel(int bufferSize) {
        Future<PushbackBuffer> future = new Future<>();

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        Tuple<Long, TimeUnit> readTimeout = config.getReadTimeout();

        CompletionHandler<Integer, Void> handler = new CompletionHandler<>() {
            @Override
            public void completed(Integer bytesRead, Void attachment) {
                if (bytesRead == -1) {
                    System.out.println("Connection closed");
                    return;
                }

                byte[] data = new byte[bytesRead];
                buffer.flip();
                buffer.get(data);

                ByteArrayInputStream stream = new ByteArrayInputStream(data);

                future.complete(new PushbackBuffer(stream));
            }

            @Override
            public void failed(Throwable error, Void attachment) {
                System.out.println("Failed to read from channel: " + error.getMessage());
                future.fail(error);
            }
        };

        channel.read(buffer, readTimeout.getFirst(), readTimeout.getSecond(), null, handler);
        return future;
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
     * Get the binary body of the request.
     */
    @Override
    public byte @NotNull [] binary() {
        return binary;
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
