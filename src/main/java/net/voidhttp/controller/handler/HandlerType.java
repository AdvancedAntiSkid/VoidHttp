package net.voidhttp.controller.handler;

import net.voidhttp.controller.dto.Dto;

/**
 * Represents an enum of the registered parameter handler types
 */
public enum HandlerType {
    /**
     * BODY indicates, that the parameter should be resolved from the request body
     * using the specified Data Transfer Object class. The parameter type class must annotate {@link Dto}.
     */
    BODY,

    /**
     * JSON indicates, that the parameter should be resolved from the request data
     * and parsed as JSON.
     */
    JSON,

    /**
     * TEXT indicates, that the parameter should be resolved from the raw request body.
     */
    TEXT,

    /**
     * REQUEST indicates, that the parameter should be resolved from the request context.
     */
    REQUEST,

    /**
     * RESPONSE indicates, that the parameter should be resolved from the response context.
     */
    RESPONSE,

    /**
     * PARAMS indicates, that the parameter should be resolved from the request url parameters.
     */
    PARAMS,

    /**
     * QUERY indicates, that the parameter should be resolved from the request query.
     */
    QUERY,

    /**
     * HEADERS indicates, that the parameter should be resolved from the request data.
     */
    HEADERS,

    /**
     * COOKIES indicates, that the parameter should be resolved from the request cookies.
     */
    COOKIES,

    /**
     * SESSION indicates, that the parameter should be resolved from the request session.
     */
    SESSION,

    /**
     * DATA indicates, that the parameter should be resolved from the
     * passed data throughout the requests.
     */
    DATA,
}
