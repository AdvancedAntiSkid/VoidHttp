package net.voidhttp.request.form;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents an entry of a multipart form data.
 */
public interface FormEntry {
    /**
     * Get the headers of the entry.
     * @return form entry headers
     */
    @NotNull Map<String, String> headers();

    /**
     * Get the name of the entry.
     * @return form entry name
     */
    @NotNull String name();

    /**
     * Get the data of the entry.
     * @return form entry data
     */
    @NotNull String data();
}
