package net.voidhttp.router;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.voidhttp.request.HttpRequest;
import net.voidhttp.request.Method;
import net.voidhttp.response.HttpResponse;

/**
 * Represents an HTTP request execution context that holds the client request and the server response.
 */
@RequiredArgsConstructor
@Getter
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
    @Setter
    private Method method;

    /**
     * The url of the client request.
     */
    @Setter
    private String url;
}
