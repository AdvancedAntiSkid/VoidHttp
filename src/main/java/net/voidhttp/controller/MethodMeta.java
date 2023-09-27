package net.voidhttp.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.voidhttp.request.Method;

/**
 * Represents a http method metadata information wrapper.
 */
@RequiredArgsConstructor
@Getter
public class MethodMeta {
    /**
     * The http method that is registered to the route listener.
     */
    private final Method method;

    /**
     * The relative route path that is registered to the route listener.
     */
    private final String value;
}
