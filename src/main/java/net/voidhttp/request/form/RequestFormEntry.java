package net.voidhttp.request.form;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents an implementation for an entry of a multipart form data.
 */
@RequiredArgsConstructor
@ToString
public class RequestFormEntry implements FormEntry {
    /**
     * The headers of the entry.
     */
    private final Map<String, String> headers;

    /**
     * The data of the entry.
     */
    private final String data;

    /**
     * The name attribute of the entry.
     */
    private final String name;

    /**
     * Get the headers of the entry.
     * @return form entry headers
     */
    @Override
    public @NotNull Map<String, String> headers() {
        return headers;
    }

    /**
     * Get the data of the entry.
     * @return form entry data
     */
    @Override
    public @NotNull String data() {
        return data;
    }

    /**
     * Get the name of the entry.
     * @return form entry name
     */
    @Override
    public @NotNull String name() {
        return name;
    }

    /**
     * Parse the multipart form data from the given lines.
     * @param lines multipart form data lines
     * @return parsed multipart form data
     */
    public static RequestFormEntry parse(List<String> lines) {
        Map<String, String> headers = new HashMap<>();
        StringBuilder data = new StringBuilder();

        boolean dataStarted = false;
        String name = "";

        // read the remaining lines of the multipart form entry
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext()) {
            String line = iterator.next();

            // handle headers whilst the entry data has not started
            if (!dataStarted) {
                // an empty line indicates, that the entry data has started
                if (line.isEmpty()) {
                    dataStarted = true;
                    continue;
                }

                // parse the next form entry header
                String[] header = line.split(":", 2);

                String key = header[0];
                String value = header[1].trim();

                headers.put(key, value);

                // handle form entry metadata
                if (!key.equals("Content-Disposition"))
                    continue;

                // resolve the "name" attribute of the form entry
                String[] disposition = value.split(";");
                for (String pair : disposition) {
                    String[] entry = pair.split("=", 2);
                    if (entry.length != 2)
                        continue;

                    String entryKey = entry[0].trim();
                    String entryValue = entry[1].trim();
                    if (entryKey.equals("name"))
                        name = entryValue.substring(1, entryValue.length() - 1);
                }

            }

            // parse the data of the entry
            else {
                data.append(line);
                if (iterator.hasNext())
                    data.append('\n');
            }
        }

        return new RequestFormEntry(headers, data.toString(), name);
    }
}
