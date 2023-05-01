package net.voidhttp;

import net.voidhttp.config.Flag;
import net.voidhttp.request.HttpRequest;
import net.voidhttp.request.query.RequestQuery;
import net.voidhttp.response.HttpResponse;
import net.voidhttp.router.Context;
import net.voidhttp.router.Middleware;
import net.voidhttp.request.Method;
import net.voidhttp.router.Route;
import net.voidhttp.router.Router;
import net.voidhttp.util.console.Logger;
import net.voidhttp.util.threading.Threading;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Represents an advanced multithreaded HTTP server.
 */
public class HttpServer {
    /**
     * The per-method routes' handler of the server.
     */
    private final Router router = new Router(this);

    /**
     * Determine if the server is running.
     */
    private volatile boolean running;

    /**
     * The server configuration flags.
     */
    private int flags;

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
     * Enable server configuration flags.
     * @param flags config flags to enable
     */
    public HttpServer enableFlags(Flag... flags) {
        for (Flag flag : flags)
            this.flags |= flag.getId();
        return this;
    }

    /**
     * Disable server configuration flags.
     * @param flags config flags to disable
     */
    public HttpServer disableFlags(Flag... flags) {
        for (Flag flag : flags)
            this.flags &= ~flag.getId();
        return this;
    }

    /**
     * Determine if the server has a configuration flag enabled.
     * @param flag flag to check
     * @return true if the flag is enabled
     */
    public boolean hasFlag(Flag flag) {
        return (flags & flag.getId()) > 0;
    }

    /**
     * Start the HTTP server and begin listening for requests.
     * @param port server port
     * @param actions server startup handlers
     */
    public void listen(int port, Runnable... actions) {
        // check if the server is already running
        if (running)
            throw new IllegalStateException("Server is already running");
        // create a new server socket
        try (ServerSocket server = new ServerSocket(port)) {
            running = true;
            // notify startup actions
            for (Runnable action : actions)
                action.run();
            // start listening for requests
            while (running) {
                // accept the next client connection
                // block whilst a new client connects
                acceptConnection(server.accept());
            }
        } catch (IOException e) {
            Logger.error("Error whilst trying to start webserver.");
            e.printStackTrace();
        }
        System.out.println("[VoidHttp] Server shut down.");
    }

    /**
     * Accept the next client socket connection.
     * @param socket connecting client
     */
    private void acceptConnection(Socket socket) {
        // create a new thread for handling the request
        ExecutorService executor = Threading.createWithId("request-thread-$code");
        executor.execute(() -> {
            // create the request and the response
            HttpRequest request = new HttpRequest(socket);
            HttpResponse response = new HttpResponse(this, socket);
            // open the request
            request.open(((method, url) -> {
                // create the execution context wrapper
                Context context = new Context(request, response, method, url);
                // handle the parsed request
                try {
                    handleRequest(context);
                } catch (Exception e) {
                    router.handleError(context, e);
                }
            }));
        });
    }

    /**
     * Asynchronously start the HTTP server and begin listening for requests.
     * @param port server port
     * @param actions server startup handlers
     */
    public void listenAsync(int port, Runnable... actions) {
        ExecutorService executor = Threading.create("listener-thread");
        executor.execute(() -> {
            // start the webserver and block this thread until shutdown
            listen(port, actions);
            // server has stopped, terminate the executor
            Threading.terminate(executor);
        });
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
     * Stop the HTTP server and close connections.
     */
    public void shutdown() {
        running = false;
    }
}
