package net.voidhttp.request;

import net.voidhttp.ServerConfig;

/**
 * Represents the current state of the request processing.
 */
public enum ReadState {
    /**
     * `HEADERS_START` is the initial state of the request processing, it means the worker will
     * begin reading the headers from the socket.
     */
    HEADERS_START,

    /**
     * `HEADERS_CONTINUE` indicates that the initial {@link ServerConfig#getHeaderReadSize()} chunk was not enough
     * to process the headers, so the worker will read another chunk from the socket.
     */
    HEADERS_CONTINUE,

    /**s
     * `HEADERS_PARSE` indicate that the worker has finished reading the headers from the socket, and the worker
     * will begin parsing the raw header data.
     */
    HEADERS_PARSE,

    /**
     * `SIZED_CONTENT_START` indicates that the worker has finished parsing the headers, and the worker will begin
     * reading the content from the socket in chunks of {@link ServerConfig#getContentReadSize()}.
     */
    SIZED_CONTENT_START,

    /**
     * `SIZED_CONTENT_CONTINUE` indicates that the initial {@link ServerConfig#getContentReadSize()} chunk was not
     * enough to process the content, so the worker will read another chunk from the socket.
     */
    SIZED_CONTENT_CONTINUE,

    /**
     * `SIZED_CONTENT_PARSE` indicates that the worker has finished reading the content from the socket, and the worker
     * will begin parsing the raw content data.
     */
    SIZED_CONTENT_PARSE,

    /**
     * `MULTI_PART_CONTENT_START` indicates that the worker has finished parsing the headers, and the worker will begin
     * reading the content from the socket in chunks of {@link ServerConfig#getContentReadSize()}.
     */
    MULTI_PART_CONTENT_START,

    /**
     * `MULTI_PART_CONTENT_CONTINUE` indicates that the initial {@link ServerConfig#getContentReadSize()} chunk was not
     * enough to process the content, so the worker will read another chunk from the socket.
     */
    MULTI_PART_CONTENT_CONTINUE,

    /**
     * `MULTI_PART_CONTENT_PARSE` indicates that the worker has finished reading the content from the socket, and the
     * worker will begin parsing the raw content data.
     */
    MULTI_PART_CONTENT_PARSE,
}
