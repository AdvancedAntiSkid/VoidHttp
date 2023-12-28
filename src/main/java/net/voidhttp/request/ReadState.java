package net.voidhttp.request;

public enum ReadState {
    START_HEADERS,
    CONTINUE_HEADERS,
    PARSE_HEADERS,
    START_CONTENT,
    CONTINUE_CONTENT,
}
