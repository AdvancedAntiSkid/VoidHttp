package net.voidhttp.router;

import net.voidhttp.request.HttpRequest;
import net.voidhttp.request.Method;
import net.voidhttp.response.HttpResponse;

/**
 * Represents a HTTP request execution context that holds the client request and the server response.
 */
public class Context {
    /**
     * The http request sent by the client.
     */
    private final HttpRequest request;

    /**
     * The http response that will be sent by the server.
     */
    private final HttpResponse response;

    /**
     * The http method used by the request.
     */
    private final Method method;

    /**
     * The url of the client request.
     */
    private final String url;

    /**
     * Initialize the http request execution context.
     * @param request client http request
     * @param response server http response
     * @param method http request method
     * @param url http request url
     */
    public Context(HttpRequest request, HttpResponse response, Method method, String url) {
        this.request = request;
        this.response = response;
        this.method = method;
        this.url = url;
    }

    /**
     * Get the http request sent by the client.
     * @return client http request data
     */
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * Get the http response that will be sent by the server.
     * @return server http response sender
     */
    public HttpResponse getResponse() {
        return response;
    }

    /**
     * Get the http method used by the request.
     * @return client request type
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Get the url of the client request.
     * @return raw request url
     */
    public String getUrl() {
        return url;
    }
}
