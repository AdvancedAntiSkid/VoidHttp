package net.voidhttp.config;

public enum Flag {
    /**
     * This server flag disables the server type to be displayed in response headers.
     */
    NO_SERVER_NAME(0x000001),

    /**
     * This server flag disables stack traces to be sent if an error occurs.
     */
    NO_STACK_TRACE(0x000002);

    private final int id;

    Flag(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
