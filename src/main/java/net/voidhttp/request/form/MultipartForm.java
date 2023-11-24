package net.voidhttp.request.form;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a wrapper for a multipart/form-data request body.
 */
public interface MultipartForm {
    /**
     * The list of the separate entries of the multipart form.
     * @return multipart form entries
     */
    @NotNull List<FormEntry> entries();

    /**
     * Get a form entry by its name attribute.
     * @param name the name of the form entry
     * @return the form entry or null if not found
     */
    @Nullable FormEntry get(@NotNull String name);
}
