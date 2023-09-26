package net.voidhttp.controller.middleware;

public @interface Middleware {
    Class<? extends CharSequence> value();
}
