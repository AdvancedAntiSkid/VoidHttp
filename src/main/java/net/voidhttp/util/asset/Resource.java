package net.voidhttp.util.asset;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a VoidHttp resource file manager.
 */
public class Resource {
    /**
     * The map of the cached resources.
     */
    private static final Map<String, Cache> resourceCache = new HashMap<>();

    /**
     * Get the resource file from cache.
     * @param resource resource name
     * @return resource content
     */
    public static byte[] get(String resource) {
        synchronized (resourceCache) {
            // get the resource from cache
            Cache cache = resourceCache.get(resource);
            if (cache != null)
                return cache.content;
            // cache not found; load the resource file
            byte[] bytes = load(resource);
            // cache the resource file
            resourceCache.put(resource, new Cache(bytes));
            return bytes;
        }
    }

    /**
     * Load a resource file from the jar.
     * @param resource resource name
     * @return resource content
     */
    public static byte[] load(String resource) {
        // get the input stream of the resource file
        InputStream stream = Resource.class.getClassLoader().getResourceAsStream(resource.substring(1));
        if (stream == null)
            return new byte[0];
        // load the content of the file
        try {
            byte[] bytes = ByteStreams.toByteArray(stream);
            // close the reader
            stream.close();
            return bytes;
        }
        // handle invalid file read
        catch (IOException e) {
            return new byte[0];
        }
    }

    /**
     * Clear the caches that are older than the given time.
     * @param time cache time in millis
     */
    public static boolean clearCache(long time) {
        long now = System.currentTimeMillis();
        synchronized (resourceCache) {
            return resourceCache.entrySet().removeIf(entry -> now - entry.getValue().timestamp > time);
        }
    }

    /**
     * Represents a cached file.
     */
    public static class Cache {
        /**
         * The content of the file.
         */
        private final byte[] content;

        /**
         * The creation timestamp of the cache.
         */
        private final long timestamp;

        /**
         * Initialize file cache.
         * @param content file content
         */
        public Cache(byte[] content) {
            this.content = content;
            timestamp = System.currentTimeMillis();
        }

        /**
         * Get the content of the file.
         */
        public byte[] getContent() {
            return content;
        }

        /**
         * Get the creation timestamp of the cache.
         */
        public long getTimestamp() {
            return timestamp;
        }
    }
}
