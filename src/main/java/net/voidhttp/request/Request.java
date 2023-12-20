package net.voidhttp.request;

import com.google.gson.JsonObject;
import net.voidhttp.header.Headers;
import net.voidhttp.request.cookie.Cookies;
import net.voidhttp.request.data.Data;
import net.voidhttp.request.form.MultipartForm;
import net.voidhttp.request.parameter.Parameters;
import net.voidhttp.request.query.Query;
import net.voidhttp.request.session.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;

/**
 * Represents a client HTTP request.
 */
public interface Request {
    /**
     * Get the requested url.
     */
    @NotNull String route();

    /**
     * Get the HTTP request method used.
     */
    @NotNull Method method();

    /**
     * The current handler has passed executing.
     * Mark the request as done.
     */
    void next();

    /**
     * Reset the request pass state.
     */
    void reset();

    /**
     * Determine if the handler has passed executing.
     */
    boolean passed();

    /**
     * Get the address of the requesting client.
     * @return ip address
     */
    @NotNull InetAddress host();

    /**
     * Get the registry of the requested headers.
     */
    @NotNull Headers headers();

    /**
     * Get the registry of the request cookies.
     */
    @NotNull Cookies cookies();

    /**
     * Get the registry of the passed values.
     */
    @NotNull Data data();

    /**
     * Get the binary body of the request.
     */
    byte @NotNull [] binary();

    /**
     * Get the request body content.
     */
    @Nullable String body();

    /**
     * Get the request body json.
     */
    @Nullable JsonObject json();

    /**
     * Get the parsed multipart/form-data body of the request.
     */
    @Nullable MultipartForm multipart();

    /**
     * Get the request parameters.
     */
    @NotNull Parameters parameters();

    /**
     * Get the current request session.
     */
    @Nullable Session session();

    /**
     * The query data of the url.
     */
    @NotNull Query query();

    /**
     * Set the current request session.
     */
    void setSession(@Nullable Session session);
}
