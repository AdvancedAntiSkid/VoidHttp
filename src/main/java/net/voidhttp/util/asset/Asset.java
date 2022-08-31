package net.voidhttp.util.asset;

import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a VoidHttp asset file manager.
 */
public class Asset {
    /**
     * The map of the cached assets.
     */
    private static final Map<String, Cache> assetCache = new HashMap<>();

    /**
     * Get the asset file content from cache.
     * @param asset asset name
     * @return asset content
     */
    public static byte[] get(String asset) {
        synchronized (assetCache) {
            // get the resource from cache
            Cache cache = assetCache.get(asset);
            if (cache != null)
                return cache.content;
            // cache not found; load the resource file
            byte[] bytes = load(asset);
            // cache the resource file
            assetCache.put(asset, new Cache(bytes));
            return bytes;
        }
    }

    /**
     * Get the asset file content from cache as string.
     * @param asset asset name
     * @return asset content
     */
    public static String getUTF(String asset) {
        return new String(get(asset), StandardCharsets.UTF_8);
    }

    /**
     * Load an asset file from the working directory.
     * @param asset asset name
     * @return asset content
     */
    public static byte[] load(String asset) {
        try {
            // get the input stream of the asset file
            InputStream stream = new FileInputStream(asset);
            // load the content of the file
            byte[] bytes = ByteStreams.toByteArray(stream);
            // close the reader
            stream.close();
            return bytes;
        }
        // handle invalid file read
        catch (IOException e) {
            // TODO send 404
            return new byte[0];
        }
    }

    /**
     * Load an asset file from the working directory as string.
     * @param asset asset name
     * @return asset content
     */
    public static String loadUTF(String asset) {
        return new String(load(asset), StandardCharsets.UTF_8);
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
