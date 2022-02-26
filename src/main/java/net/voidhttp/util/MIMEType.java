package net.voidhttp.util;

public enum MIMEType {
    PLAIN_TEXT("text/plain"),
    CSS("text/css"),
    HTML("text/html"),
    JAVASCRIPT("text/html"),
    JSON("application/json"),
    PNG("image/png"),
    JPG("image/jpg"),
    GIF("image/gif"),
    UNKNOWN("unknown");

    /**
     * The content type displayed in the header.
     */
    private final String type;

    /**
     * Initialize the mime type.
     * @param type content type
     */
    MIMEType(String type) {
        this.type = type;
    }

    /**
     * Get the content type displayed in the header.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the content type displayed in the header.
     */
    @Override
    public String toString() {
        return type;
    }

    /**
     * Get the MIME type from file extension.
     * @param extension file extension
     * @return found MIME type
     */
    public static MIMEType fromExtension(String extension) {
        switch (extension) {
            case ".txt":
                return PLAIN_TEXT;
            case ".css":
                return CSS;
            case ".html":
                return HTML;
            case ".js":
                return JAVASCRIPT;
            case ".png":
                return PNG;
            case ".jpg":
                return JPG;
            case ".gif":
                return GIF;
            case ".json":
                return JSON;
            default:
                return UNKNOWN;
        }
    }

    /**
     * Get the MIME type from extension or default value.
     * @param extension file extension
     * @param defaultValue value to return if MIME type not found
     * @return MIME type
     */
    public static MIMEType fromExtensionOrDefault(String extension, MIMEType defaultValue) {
        MIMEType type = fromExtension(extension);
        if (type != UNKNOWN)
            return type;
        return defaultValue;
    }
}
