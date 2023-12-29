package net.voidhttp;

import dev.inventex.octa.console.Logger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Represents a pool of concurrent socket channels. Each socket channel is cached here
 * until they are processed by a worker thread.
 */
@RequiredArgsConstructor
public class SocketChannelPool {
    /**
     * The asynchronous server socket channel of the server.
     */
    private final AsynchronousServerSocketChannel server;

    /**
     * The configuration of the http server.
     */
    private final ServerConfig config;

    /**
     * The handler for processing successful socket channel connections.
     */
    private final Consumer<AsynchronousSocketChannel> acceptHandler;

    /**
     * The concurrent set of socket channels.
     */
    private Set<AsynchronousSocketChannel> channels;

    /**
     * The last time the server notified the user about the max concurrent connections.
     */
    private long lastNotify = -1;

    /**
     * Accept the next incoming socket channel connection.
     * @param channel the socket channel to accept
     */
    @SneakyThrows
    private void acceptChannel(AsynchronousSocketChannel channel) {
        // accept the channel if the server can handle more connections
        if (channels.size() < config.getMaxConcurrentConnections()) {
            channels.add(channel);
            acceptHandler.accept(channel);
            return;
        }

        // reject the channel if the server cannot handle more connections
        channel.close();

        // notify the user about the max concurrent connections
        if (lastNotify < 0 || System.currentTimeMillis() - lastNotify > 1000) {
            Logger.error("[VoidHttp] Max concurrent connections reached.");
            lastNotify = System.currentTimeMillis();
        }
    }

    /**
     * Release the given socket channel from the pool after it has been processed.
     * @param channel the socket channel to release
     */
    public void releaseChannel(AsynchronousSocketChannel channel) {
        channels.remove(channel);
    }

    /**
     * Accept incoming client socket connections recursively.
     */
    public void acceptSockets() {
        channels = Collections.newSetFromMap(new ConcurrentHashMap<>(
            config.getMaxConcurrentConnections()
        ));

        // accept the first connection
        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            /**
             * Invoked when an operation has completed.
             *
             * @param channel the result of the I/O operation
             * @param attachment the object attached to the I/O operation when it was initiated
             */
            @Override
            public void completed(AsynchronousSocketChannel channel, Void attachment) {
                // accept the next connection recursively, if the server is still open
                if (server.isOpen())
                    server.accept(null, this);

                acceptChannel(channel);
            }

            /**
             * Invoked when an operation fails.
             *
             * @param exc the exception to indicate why the I/O operation failed
             * @param attachment the object attached to the I/O operation when it was initiated
             */
            @Override
            public void failed(Throwable exc, Void attachment) {
                System.err.println("[VoidHttp] Failed to accept connection: " + exc.getMessage());
            }
        });
    }
}
