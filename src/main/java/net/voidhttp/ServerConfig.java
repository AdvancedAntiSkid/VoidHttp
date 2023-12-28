package net.voidhttp;

import dev.inventex.octa.data.primitive.Tuple;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * Represents a wrapper for the VoidHttp server configurations.
 * Each setting has a default value, which is configured to work properly in most cases.
 * <p>
 * Make sure to only change these if you know what you are doing.
 */
@Data
public class ServerConfig {
    /**
     * The amount of bytes to be read from the client socket channel when reading the headers.
     * Initially, VoidHttp will read the first specified amount of bytes from the socket.
     * If the headers are not finished, it will read another chunk of data from the socket.
     * <p>
     * The reader may read more than the {@link #maxHeaderSize} setting, but exceeding
     * this limit will drop the connection.
     */
    private int headerReadSize = 4096;

    /**
     * The maximum size in bytes of the headers. If the socket exceeds this limit, the connection is dropped.
     * <p>
     * Standard HTTP servers use values similar to this.
     * Apache uses 8KB, IIS uses 16KB.
     * <p>
     * For standard browser requests, this should be more than enough.
     */
    private int maxHeaderSize = 8192;

    /**
     * The size in bytes, of each chunk of data that is read from the socket when parsing the content.
     * <p>
     * This will be used when the `Content-Length` is not guaranteed (for example a `form-data` request), or
     * if the `Content-Length` exceeds the specified contend buffer size.
     * <p>
     * In that case, VoidHttp will read the content from the socket in chunks of this size.
     */
    private int contentReadSize = 131072;

    /**
     * The maximum size in bytes of the content. If the socket exceeds this limit, the connection is dropped.
     * <p>
     * Legitimate requests should not exceed this limit, as it is very high.
     */
    private int maxContentLength = 1048576;

    /**
     * The number of threads in the thread pool.
     * <p>
     * This setting is ignored if {@link #virtualThreads} is enabled.
     * <p>
     * On the webserver startup, VoidHttp will initialize a thread pool using the specified amount of OS threads.
     * <p>
     * Changing this value will not affect if the server is already running.
     */
    private int poolSize = Runtime.getRuntime().availableProcessors();

    /**
     * The indication, whether virtual threads should be used instead of the standard OS threads.
     * For most cases, this should be enabled, as it is more efficient, but keep in mind, this way
     * the threads are completely auto-scaled and are managed by the JVM.
     * <p>
     * If this setting is enabled, the {@link #poolSize} setting is ignored.
     */
    private boolean virtualThreads = true;

    /**
     * The maximum timeout for reading data from the client socket channel.
     * <p>
     * If the client does not send any data within the specified timeout, the connection is dropped.
     */
    private Tuple<Long, TimeUnit> readTimeout = new Tuple<>(10L, TimeUnit.SECONDS);

    /**
     * The maximum amount of concurrent connections that are processed by VoidHttp.
     * <p>
     * If the server exceeds this limit, new connection will be dropped until the old ones are processed.
     * <p>
     * Make sure to set this value according to your hardware capabilities.
     * A webserver cannot physically handle an infinite amount of connections, as it will run out of resources
     * or the requests will be queued for too long.
     */
    private int maxConcurrentConnections = 10_000;

    /**
     * The indication, whether the server provider should be displayed in the response headers.
     * <p>
     * Unless you don't want users to be able to identify the server, this can be enabled.
     */
    private boolean serverNameDisplay = true;

    /**
     * The indication, whether stack traces should be sent to the client if an error occurs.
     * <p>
     * This option is useful for debugging for development purposes, but is strongly discouraged for production.
     */
    private boolean sendStackTrace = true;
}
