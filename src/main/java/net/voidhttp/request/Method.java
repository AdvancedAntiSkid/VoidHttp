package net.voidhttp.request;

/**
 * HTTP defines a set of request methods to indicate the desired action to be performed for a given resource.
 * Although they can also be nouns, these request methods are sometimes referred to as HTTP verbs.
 * Each of them implements a different semantic, but some common features are shared by a group of them:
 * e.g. a request method can be safe, idempotent, or cacheable.
 */
public enum Method {
    /**
     * The GET method requests a representation of the specified resource.
     * Requests using GET should only retrieve data.
     */
    GET,

    /**
     * The HEAD method asks for a response identical to a GET request, but without the response body.
     */
    HEAD,

    /**
     * The POST method submits an entity to the specified resource, often causing a change
     * in state or side effects on the server.
     */
    POST,

    /**
     * The PUT method replaces all current representations of the target resource with the request payload.
     */
    PUT,

    /**
     * The DELETE method deletes the specified resource.
     */
    DELETE,

    /**
     * The CONNECT method establishes a tunnel to the server identified by the target resource.
     */
    CONNECT,

    /**
     * The OPTIONS method describes the communication options for the target resource.
     */
    OPTIONS,

    /**
     * The TRACE method performs a message loop-back test along the path to the target resource.
     */
    TRACE,

    /**
     * The PATCH method applies partial modifications to a resource.
     */
    PATCH,

    /**
     * The method was unrecognized.
     */
    UNKNOWN;

    /**
     * Get the request method with the given name.
     * @param name request method name
     * @return found request method
     */
    public static Method of(String name) {
        // convert the name to lowercase
        name = name.toLowerCase();
        // loop through the method values
        for (Method method : values()) {
            // check if the method name matches
            if (method.name().toLowerCase().equals(name))
                return method;
        }
        // method not found
        return UNKNOWN;
    }
}
