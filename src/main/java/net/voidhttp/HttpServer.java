package net.voidhttp;

import lombok.Getter;
import lombok.SneakyThrows;
import net.voidhttp.controller.ControllerInjector;
import net.voidhttp.request.HttpRequest;
import net.voidhttp.request.query.RequestQuery;
import net.voidhttp.response.HttpResponse;
import net.voidhttp.router.Context;
import net.voidhttp.router.Middleware;
import net.voidhttp.request.Method;
import net.voidhttp.router.Route;
import net.voidhttp.router.Router;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * Represents an advanced multithreaded HTTP server.
 */
public class HttpServer {
    /**
     * The per-method routes' handler of the server.
     */
    private final Router router = new Router(this);

    /**
     * The dynamic route controller injector of the server.
     */
    private final ControllerInjector injector = new ControllerInjector();

    /**
     * The asynchronous server socket channel of the server.
     */
    private AsynchronousServerSocketChannel server;

    /**
     * The configuration of the http server.
     */
    @Getter
    private ServerConfig config = new ServerConfig();

    /**
     * The concurrent socket channel pool of the server.
     */
    private SocketChannelPool channelPool;

    /**
     * Register a handler for the given request method.
     * @param method request method
     * @param route request route
     * @param middlewares request handlers
     */
    public HttpServer register(Method method, String route, Middleware... middlewares) {
        router.register(method, route, middlewares);
        return this;
    }

    /**
     * Register a GET request handler.
     * @param route request route
     * @param middlewares request handlers
     */
    public HttpServer get(String route, Middleware... middlewares) {
        router.register(Method.GET, route, middlewares);
        return this;
    }

    /**
     * Register a POST request handler.
     * @param route request route
     * @param middlewares request handler
     */
    public HttpServer post(String route, Middleware... middlewares) {
        router.register(Method.POST, route, middlewares);
        return this;
    }

    /**
     * Register a request error handler.
     * @param code error code
     * @param middlewares error handlers
     */
    public HttpServer error(int code, Middleware... middlewares) {
        router.error(code, middlewares);
        return this;
    }

    /**
     * Register a global request handler.
     * @param middlewares global handlers
     */
    public HttpServer use(Middleware... middlewares) {
        router.use(middlewares);
        return this;
    }

    /**
     * Update the configuration of the http server.
     * @param config the new configuration
     * @return the server instance
     */
    public HttpServer config(ServerConfig config) {
        this.config = config;
        return this;
    }

    /**
     * Inject an HTTP route controller into the server.
     * @param controller controller to inject
     * @param <T> type of the controller
     */
    public <T> HttpServer inject(T controller) {
        injector.inject(this, controller);
        return this;
    }

    /**
     * Start the HTTP server and begin listening for requests.
     * @param port server port
     * @param actions server startup handlers
     */
    @SneakyThrows
    public void listen(int port, Runnable... actions) {
        // check if the server is already running
        if (isRunning())
            throw new IllegalStateException("Server is already running");

        // create the server socket channel and bind it to the specified port
        server = AsynchronousServerSocketChannel.open(createChannelGroup());
        server.bind(new InetSocketAddress("127.0.0.1", port), 1000);

        // accept incoming socket connections
        channelPool = new SocketChannelPool(
            server,
            config,
            this::acceptConnection
        );
        channelPool.acceptSockets();

        // notify startup actions
        for (Runnable action : actions)
            action.run();
    }

    /**
     * Create the asynchronous channel group for the server that will take care of
     * balancing the incoming connections between the threads.
     *
     * @return the asynchronous channel group
     */
    @SneakyThrows
    private AsynchronousChannelGroup createChannelGroup() {
        return AsynchronousChannelGroup.withThreadPool(
            // in case of virtual threads, we should not pool them
            // for more information, visit: https://docs.oracle.com/en/java/javase/20/core/virtual-threads.html#GUID-9065C2D5-9006-4F1A-93E0-D5153BB40475
            config.isVirtualThreads() ?
                Executors.newVirtualThreadPerTaskExecutor() :
                Executors.newFixedThreadPool(config.getPoolSize())
        );
    }

    /**
     * Accept the next client socket connection.
     * @param channel connecting client socket channel
     */
    private void acceptConnection(AsynchronousSocketChannel channel) {
        // create the request and the response
        HttpRequest request = new HttpRequest(channel, config);
        HttpResponse response = new HttpResponse(this, channel);

        // create the execution context wrapper
        Context context = new Context(request, response);

        // read the request from the incoming socket
        request
            .parse()
            .tryThen(val -> {
                // update the request data
                context.setMethod(request.method());
                context.setUrl(request.route());

                // let the router handle the request
                handleRequest(context);
            }).except(e -> {
                // redirect the error to the router, let implementation handle it
                router.handleError(context, e);
            }).result((BiConsumer<Void, Throwable>) (val, err) -> channelPool.releaseChannel(channel));
        // TODO release the channel after the write operation is done
    }

    /**
     * Handle the incoming parsed request.
     * @param context http request execution context
     * @throws Exception error whilst processing
     */
    private void handleRequest(Context context) throws Exception {
        // extract the request and response of the context
        HttpRequest request = context.getRequest();
        HttpResponse response = context.getResponse();

        // get the necessary request data
        Method method = request.method();
        String url = request.route();

        // preprocess the router middlewares
        router.preprocess(context);

        // get the list of routes corresponding for the method
        List<Route> routes = router.getRoutes(method);
        // check if there aren't any handlers for the method
        if (routes == null || routes.isEmpty()) {
            router.handleNotFound(context);
            return;
        }

        // declare a variable for determining if the request was handled or not
        // so we can send a 404 error
        boolean handled = false;
        // handle the routes registered for the method
        for (Route route : routes) {
            // continue if the route did not pass the test
            RequestQuery query = new RequestQuery();
            if (!route.test(url, query))
                continue;

            // handle the request
            request.setQuery(query);
            route.handle(request, response);

            // stop processing if the handler did not pass the handling
            if (!request.passed())
                return;

            // mark the request as handled and reset the request state
            handled = true;
            request.reset();
        }

        // check for 404 error handlers that will override
        // this is the default "not found" handler
        if (!handled)
            router.handleNotFound(context);
    }

    /**
     * Indicate, whether the server is running or not.
     * @return true if the server is running
     */
    public boolean isRunning() {
        return server != null && server.isOpen();
    }

    /**
     * Stop the HTTP server and close connections.
     */
    @SneakyThrows
    public void shutdown() {
        server.close();
    }
}
