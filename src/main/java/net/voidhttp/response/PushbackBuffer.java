package net.voidhttp.response;

import dev.inventex.octa.data.primitive.Tuple;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Represents a buffer that is used to process a chunk that is read from a socket channel.
 * <p>
 * This buffer is used to read the headers and the content of the request.
 */
public class PushbackBuffer extends PushbackInputStream {
    /**
     * The amount of bytes that are read from the chunk.
     */
    private final int size;

    /**
     * Creates a {@code PushbackInputStream}
     * with a 1-byte pushback buffer, and saves its argument, the input stream
     * {@code in}, for later use. Initially,
     * the pushback buffer is empty.
     *
     * @param in the input stream from which bytes will be read.
     */
    @SneakyThrows
    public PushbackBuffer(InputStream in) {
        super(in);
        size = in.available();
    }

    /**
     * Read the next line from a binary input stream.
     * @return the next line
     * @throws IOException error whilst reading
     */
    public String readLine() throws IOException {
        StringBuilder builder = new StringBuilder();
        int read;
        boolean newLine = false;

        while ((read = read()) != -1) {
            char c = (char) read;

            if (c == '\n') {
                newLine = true;
                break;
            } else if (c == '\r') {
                // check for \r and handle it if encountered
                newLine = true;
                int nextChar = read(); // read the next character after \r

                if (nextChar != -1 && (char) nextChar != '\n') {
                    // if it's not \n, put it back to the stream
                    unread(nextChar);
                }
                break;
            }

            builder.append(c);
        }

        // return null if there's no more data to read
        if (!newLine && builder.isEmpty())
            return null;

        return builder.toString();
    }

    /**
     * Read the next line from a binary input stream. Handle lines that are not terminated with \n.
     * @return the next line and whether it's terminated with \n
     * @throws IOException error whilst reading
     */
    public Tuple<String, Boolean> readHeaderLine() throws IOException {
        StringBuilder builder = new StringBuilder();
        int read;
        boolean newLine = false;

        while ((read = read()) != -1) {
            char c = (char) read;

            if (c == '\n') {
                newLine = true;
                break;
            } else if (c == '\r') {
                // check for \r and handle it if encountered
                newLine = true;
                int nextChar = read(); // read the next character after \r

                if (nextChar != -1 && (char) nextChar != '\n') {
                    // if it's not \n, put it back to the stream
                    unread(nextChar);
                }
                break;
            }

            builder.append(c);
        }

        // return null if there's no more data to read
        if (!newLine && builder.isEmpty())
            return null;

        return new Tuple<>(builder.toString(), newLine);
    }

    public int position() {
        return pos;
    }

    public int size() {
        return size;
    }
}
