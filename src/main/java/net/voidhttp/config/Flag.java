package net.voidhttp.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Flag {
    /**
     * This server flag disables the server type to be displayed in response headers.
     */
    NO_SERVER_NAME(0x000001),

    /**
     * This server flag disables stack traces to be sent if an error occurs.
     */
    NO_STACK_TRACE(0x000002);

    /**
     * The unique identifier of the flag.
     */
    private final int id;
}
