package net.voidhttp.request;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.inventex.octa.concurrent.future.Future;
import dev.inventex.octa.console.Logger;
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

    /**
     * The future that will be completed when the request has been parsed.
     */
    private final Future<Void> completionHandler = new Future<>();

    /**
     * The list of raw header that have not been parsed yet.
     */
    private final List<String> headerLines = new ArrayList<>();

    /**
     * The last read header value, that is used to continue parsing a header if it is stuck between two chunks.
     */
    private String lastHeaderLinePart;

    /**
     * The indication, whether the last header line was incomplete.
     */
    private boolean incompleteHeader;

    /**
     * The total size in byes of the headers.
     */
    private int totalHeaderSize;

    /**
     * The buffer that will be used to store the content of the request.
     */
    private final ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();

    /**
     * The expected size in bytes of the request content. This is specified by the `Content-Length` header.
     */
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

    /**
     * Begin reading the request from the socket channel.
     * @return future that will be completed when the request has been parsed
     */
    public Future<Void> parse() {
        try {
            // read the fire chunk of the request for the headers
            nextChunk(ReadState.HEADERS_START);
        } catch (Throwable t) {
            completionHandler.fail(t);
        }
        return completionHandler;
    }

    /**
     * Parse the next chunk from the socket channel.
     * @param nextState the next type of chunk to be processed
     * @return future that will be completed when the chunk has been parsed
     */
    private Future<Void> nextChunk(ReadState nextState) {
        return switch (nextState) {
            case HEADERS_START -> handleHeaderStart();
            case HEADERS_CONTINUE -> handleHeaderContinue();
            case HEADERS_PARSE -> handleHeaderParse();
            case SIZED_CONTENT_START -> handleSizedContentStart();
            case SIZED_CONTENT_CONTINUE -> handleSizedContentContinue();
            case SIZED_CONTENT_PARSE -> handleSizedContentParse();
            default -> throw new RuntimeException("Unhandled end of handler chain: " + nextState);
        };
    }

    /**
     * Handle the start of the header reading process. Read the initial {@link ServerConfig#getHeaderReadSize()} bytes
     * from the socket channel. If the headers are not finished, read the next chunk.
     * @return future that will be completed when the request processing is completed
     */
    private Future<Void> handleHeaderStart() {
        Future<Void> future = new Future<>();

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
                    nextChunk(ReadState.HEADERS_CONTINUE)
                        .then(future::complete)
                        .except(future::fail);
                    return;
                }

                // the headers and the request body is separated using an empty line
                // stop processing headers if the line is empty
                if (header.isEmpty()) {
                    // append the read amount of bytes to the total header size
                    totalHeaderSize += buffer.position();

                    // read all remaining bytes from the buffer
                    contentBuffer.write(buffer.readAllBytes());

                    nextChunk(ReadState.HEADERS_PARSE)
                        .then(future::complete)
                        .except(future::fail);
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
            nextChunk(ReadState.HEADERS_CONTINUE)
                .then(future::complete)
                .except(future::fail);
        }).except(future::fail);

        return future;
    }

    /**
     * Handle the continuation of the header reading process, because the initial chunk couldn't fit the entire
     * headers. Read the next {@link ServerConfig#getHeaderReadSize()} chunk from the socket channel.
     * @return future that will be completed when the request processing is completed
     */
    private Future<Void> handleHeaderContinue() {
        // before reading the next header chunk, check if the header size has not exceeded the maximum size
        if (totalHeaderSize > config.getMaxHeaderSize())
            return Future.failed(new RuntimeException(
                "Header size " + totalHeaderSize + " exceeded maximum size of " + config.getMaxHeaderSize() + " bytes"
            ));

        Future<Void> future = new Future<>();

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
                    nextChunk(ReadState.HEADERS_CONTINUE)
                        .then(future::complete)
                        .except(future::fail);
                    return;
                }

                // the headers and the request body is separated using an empty line
                // stop processing headers if the line is empty
                if (header.isEmpty()) {
                    // append the read amount of bytes to the total header size
                    totalHeaderSize += buffer.position();

                    // read all remaining bytes from the buffer
                    contentBuffer.write(buffer.readAllBytes());

                    nextChunk(ReadState.HEADERS_PARSE)
                        .then(future::complete)
                        .except(future::fail);
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
            nextChunk(ReadState.HEADERS_CONTINUE)
                .then(future::complete)
                .except(future::fail);
        }).except(future::fail);

        return future;
    }

    /**
     * Handle the parsing of the headers. The headers have been read, now parse them.
     * @return future that will be completed when the request processing is completed
     */
    private Future<Void> handleHeaderParse() {
        Future<Void> future = new Future<>();

        Future.tryComplete(() -> {
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

            // do not parse the content if the request does not have a content type, there is no content
            if (!headers.has("content-type")) {
                completionHandler.complete(null);
                return null;
            }

            if (headers.get("content-type").equals("multipart/form-data"))
                throw new RuntimeException("multipart/form-data not implemented yet");

            handleSizedContentStart()
                .then(future::complete)
                .except(future::fail);

            return null;
        }).except(future::fail);

        return future;
    }

    /**
     * Handle the beginning of the content reading process. Use a specific reader for the content type.
     * @return future that will be completed when the request processing is completed
     */
    private Future<Void> handleSizedContentStart() {
        if (!headers.has("content-length"))
            return Future.failed(new IllegalStateException(
                "Header `content-length` must be specified for `" + headers.get("content-type") + "` request"
            ));

        contentLength = Integer.parseInt(headers.get("content-length"));

        return handleSizedContentContinue();
    }

    /**
     * Handle the continuation of the sized content reading process. Read the next
     * {@link ServerConfig#getContentReadSize()} chunk from the socket channel. If the content length is less
     * than the specified chunk size, VoidHttp will only read the content length amount of bytes.
     * @return future that will be completed when the request processing is completed
     */
    private Future<Void> handleSizedContentContinue() {
        Future<Void> future = new Future<>();

        int remainingBytes = contentLength - contentBuffer.size();
        int readSize = Math.min(config.getContentReadSize(), remainingBytes);

        readChannel(readSize).tryThen(buffer -> {
            contentBuffer.write(buffer.readAllBytes());

            if (contentBuffer.size() > config.getMaxContentLength())
                throw new RuntimeException(
                    "Content length " + contentBuffer.size() + " exceeded maximum size of " +
                    config.getMaxContentLength() + " bytes"
                );

            Future<Void> callback = contentBuffer.size() < contentLength
                ? handleSizedContentContinue()
                : handleSizedContentParse();

            callback.then(future::complete).except(future::fail);
        }).except(future::fail);

        return future;
    }

    /**
     * Handle the parsing of the sized content. The content has been read, now parse it.
     * @return future that will be completed when the request processing is completed
     */
    private Future<Void> handleSizedContentParse() {
        return Future.tryComplete(() -> {
            binary = contentBuffer.toByteArray();
            body = contentBuffer.toString(StandardCharsets.UTF_8);

            // get the type of the requested content
            if (headers.get("content-type").equals("application/json")) {
                // parse the request body to json
                try {
                    json = (JsonObject) JsonParser.parseString(body);
                } catch (Exception ignored) {}
            }

            completionHandler.complete(null);

            return null;
        });
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

    /**
     * Read the next `bufferSize` amount of bytes from the socket channel.
     * @param bufferSize the amount of bytes to be read
     * @return the future that will be completed with the buffer that was read
     */
    private Future<PushbackBuffer> readChannel(int bufferSize) {
        Future<PushbackBuffer> future = new Future<>();

        // allocate a buffer of the specified chunk size
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        Tuple<Long, TimeUnit> readTimeout = config.getReadTimeout();

        // create a handler that will be called when the buffer has been read
        CompletionHandler<Integer, Void> handler = new CompletionHandler<>() {
            @Override
            public void completed(Integer bytesRead, Void attachment) {
                // check if the end of the stream has been reached
                if (bytesRead == -1) {
                    future.fail(new RuntimeException("End of stream reached"));
                    return;
                }

                // get the read bytes from the buffer
                byte[] data = new byte[bytesRead];
                buffer.flip();
                buffer.get(data);

                // create an input stream from the read bytes
                ByteArrayInputStream stream = new ByteArrayInputStream(data);

                // wrap the input stream in a pushback buffer
                future.complete(new PushbackBuffer(stream));
            }

            @Override
            public void failed(Throwable error, Void attachment) {
                // check if and error occurred whilst reading from the socket channel
                Logger.error("Failed to read from channel:");
                error.printStackTrace();
                future.fail(error);
            }
        };

        // read the next chunk from the socket channel using the specified timeout and handler
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
