package net.voidhttp;

import dev.inventex.octa.data.primitive.Tuple;
import lombok.Data;
import net.voidhttp.config.Flag;

import java.util.concurrent.TimeUnit;

@Data
public class ServerConfig {
    /**
     * The server configuration flags.
     */
    private int flags;

    /**
     * The size of each chunk of data that is read from the socket.
     */
    private int chunkSize = 131072;

    /**
     * The number of threads in the thread pool.
     */
    private int poolSize = Runtime.getRuntime().availableProcessors();

    /**
     * The timeout for reading data from the client socket channel.
     */
    private Tuple<Long, TimeUnit> readTimeout = new Tuple<>(10L, TimeUnit.SECONDS);

    /**
     * Enable server configuration flags.
     * @param flags config flags to enable
     */
    public ServerConfig enableFlags(Flag... flags) {
        for (Flag flag : flags)
            this.flags |= flag.getId();
        return this;
    }

    /**
     * Disable server configuration flags.
     * @param flags config flags to disable
     */
    public ServerConfig disableFlags(Flag... flags) {
        for (Flag flag : flags)
            this.flags &= ~flag.getId();
        return this;
    }

    /**
     * Indicate, whether the server has a configuration flag enabled.
     * @param flag flag to check
     * @return true if the flag is enabled
     */
    public boolean hasFlag(Flag flag) {
        return (flags & flag.getId()) > 0;
    }
}
