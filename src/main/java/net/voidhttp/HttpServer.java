package net.voidhttp;

import net.voidhttp.handler.Handler;
import net.voidhttp.handler.Route;
import net.voidhttp.request.query.Query;
import net.voidhttp.request.query.RequestQuery;
import net.voidhttp.request.HttpRequest;
import net.voidhttp.request.Method;
import net.voidhttp.response.HttpResponse;
import net.voidhttp.util.threading.Threading;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an advanced multi-threaded HTTP server.
 */
public class HttpServer {
    /**
     * The map of the registered HTTP routes.
     */
    private final Map<Method, List<Route>> routeMap = new HashMap<>();

    /**
     * The map of the registered error routes.
     */
    private final Map<Integer, List<Route>> errorMap = new HashMap<>();

    /**
     * The list of the globally used handlers.
     */
    private final List<Handler> handlerList = new ArrayList<>();

    /**
     * The increment-based thread name indicator.
     */
    private final AtomicInteger threadId = new AtomicInteger(1);

    /**
     * Determines if the server is running.
     */
    private volatile boolean running;

    /**
     * Register a handler for the given request method.
     * @param method request method
     * @param route request route
     * @param handlers request handlers
     */
    public HttpServer register(Method method, String route, Handler... handlers) {
        // get the registered routes for the method
        List<Route> routes = routeMap.getOrDefault(method, new ArrayList<>());
        // register the handlers
        routes.add(new Route(route, handlers));
        // update the routes
        routeMap.put(method, routes);
        return this;
    }

    /**
     * Register a GET request handler.
     * @param route request route
     * @param handlers request handlers
     */
    public HttpServer get(String route, Handler... handlers) {
        return register(Method.GET, route, handlers);
    }

    /**
     * Register a POST request handler.
     * @param route request route
     * @param handlers request handler
     */
    public HttpServer post(String route, Handler... handlers) {
        return register(Method.POST, route, handlers);
    }

    /**
     * Register a request error handler.
     * @param code error code
     * @param handlers error handlers
     */
    public HttpServer error(int code, Handler... handlers) {
        // get the registered routes for the error code
        List<Route> routes = errorMap.getOrDefault(code, new ArrayList<>());
        // register the handlers
        routes.add(new Route("", handlers));
        // update the routes
        errorMap.put(code, routes);
        return this;
    }

    /**
     * Register a global request handler.
     * @param handlers global handlers
     */
    public HttpServer use(Handler... handlers) {
        handlerList.addAll(Arrays.asList(handlers));
        return this;
    }

    /**
     * Start the HTTP server and begin listening for requests.
     * @param port server port
     * @param actions server startup handlers
     * @throws IOException error whilst starting up
     */
    public void listen(int port, Runnable... actions) throws IOException {
        // check if the server is already running
        if (running)
            throw new IllegalStateException("Server is already running");
        // create socket server
        ServerSocket server = new ServerSocket(port);
        running = true;
        // notify startup actions
        for (Runnable action : actions)
            action.run();
        // start listening for requests
        while (running) {
            // accept client connection
            Socket socket = server.accept();
            // create a new thread for handling the request
            ExecutorService executor = Threading.create("request-thread-" + threadId.getAndIncrement());
            executor.execute(() -> {
                // create request and response
                HttpRequest request = new HttpRequest(socket);
                HttpResponse response = new HttpResponse(socket);
                // open the request
                request.open(((method, url) -> {
                    // handle the parsed request
                    try {
                        handleRequest(request, response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
                // executing has ended, shutdown the executor
                Threading.terminate(executor);
            });
        }
        // close the socket server
        server.close();
        System.out.println("[VoidHttp] Server shut down.");
    }

    /**
     * Asynchronously start the HTTP server and begin listening for requests.
     * @param port server port
     * @param actions server startup handlers
     */
    public void listenAsync(int port, Runnable... actions) {
        ExecutorService executor = Threading.create("listener-thread");
        executor.execute(() -> {
            try {
                // start the webserver and block this thread until shutdown
                listen(port, actions);
                // server has stopped, terminate the executor
                Threading.terminate(executor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Handle the incoming parsed request.
     * @param request client request
     * @param response server response
     * @throws Exception error whilst processing
     */
    private void handleRequest(HttpRequest request, HttpResponse response) throws Exception {
        // get the necessary request data
        Method method = request.method();
        String url = request.route();
        // handle globally used middlewares
        // this is must be done before handling the method handlers
        for (Handler handler : handlerList) {
            try {
                // make the global handler handle the request
                handler.handle(request, response);
                // stop processing if the handler did not pass the handling
                if (!request.passed())
                    return;
                request.reset();
            }
            // handle an occurred error happened whilst
            // processing global middleware
            catch (Exception e) {
                response.send("error");
                // TODO send track trace
                e.printStackTrace();
                return;
            }
        }
        // get the list of routes corresponding for the method
        List<Route> routes = routeMap.get(method);
        // check if there aren't any handlers for the method
        if (routes == null || routes.isEmpty()) {
            response.send("<pre>" + "Cannot " + method + " " + url + "</pre>");
            return;
        }
        // declare a variable for determining if the request was handled or not
        // so we can send a 404 error
        boolean handled = false;
        // handle the routes registered for the method
        for (Route route : routes) {
            // continue if the route did not pass the test
            Query query = new RequestQuery();
            if (!route.test(url, query))
                continue;
            // handle the request
            request.setQuery(query);
            route.handle(request, response);
            handled = true;
            // stop processing if the handler did not pass the handling
            if (!request.passed())
                return;
            request.reset();
        }
        // check for 404 error handlers that will override
        // the default "not found" handler
        if (!handled) {
            // get the 404 handlers
            List<Route> errorRoutes = errorMap.get(404);
            // check if there are any of them
            if (errorRoutes != null) {
                // loop through the error handlers
                for (Route route : errorRoutes) {
                    route.handle(request, response);
                    // handle the 404 error
                    handled = true;
                    // stop processing if the handler did not pass the handling
                    if (!request.passed())
                        return;
                    request.reset();
                }
            }
        }
        // check if the request was not handled and there
        // weren't any 404 handlers
        if (!handled) {
            response.send("<pre>" + "Cannot " + method + " " + url + "</pre>");
        }
    }

    /**
     * Stop the HTTP server and close connections.
     */
    public void shutdown() {
        running = false;
    }
}
