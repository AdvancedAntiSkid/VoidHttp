package net.voidhttp.request.form;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a wrapper for a multipart/form-data request body.
 */
@RequiredArgsConstructor
@ToString
public class RequestMultipartForm implements MultipartForm {
    /**
     * The list of the separate entries of the multipart form.
     */
    private final List<FormEntry> entries;

    /**
     * The list of the separate entries of the multipart form.
     * @return multipart form entries
     */
    @Override
    public @NotNull List<FormEntry> entries() {
        return entries;
    }

    /**
     * Get a form entry by its name attribute.
     * @param name the name of the form entry
     * @return the form entry or null if not found
     */
    @Override
    public @Nullable FormEntry get(@NotNull String name) {
        return entries
            .stream()
            .filter(entry -> entry.name().equals(name))
            .findFirst()
            .orElse(null);
    }
}
