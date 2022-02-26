package net.voidhttp.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a VoidHttp template manager.
 */
public class Template {
    /**
     * The map of the cached templates.
     */
    private static final Map<String, Cache> templateCache = new HashMap<>();

    /**
     * Get the template file from cache.
     * @param template template name
     * @return template content
     */
    public static String get(String template) {
        synchronized (templateCache) {
            // get the template from cache
            Cache cache = templateCache.get(template);
            if (cache != null)
                return cache.content;
            // cache not found; load the template file
            String content = load(template);
            // cache the template file
            templateCache.put(template, new Cache(content));
            return content;
        }
    }

    /**
     * Load a template file from the jar.
     * @param template template name
     * @return template content
     */
    public static String load(String template) {
        // get the input stream of the resource file
        InputStream stream = Resource.class.getClassLoader().getResourceAsStream("templates/" + template + ".html");
        if (stream == null)
            throw new IllegalStateException("Template '" + template + "' does not exists.");
        // create an input reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;
        // read the content of the file
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load template '" + template + "'", e);
        }
        return builder.toString();
    }

    /**
     * Clear the caches that are older than the given time.
     * @param time cache time in millis
     */
    public static boolean clearCache(long time) {
        long now = System.currentTimeMillis();
        synchronized (templateCache) {
            return templateCache.entrySet().removeIf(entry -> now - entry.getValue().timestamp > time);
        }
    }

    /**
     * Represents a cached template.
     */
    public static class Cache {
        /**
         * The content of the template.
         */
        private final String content;

        /**
         * The creation timestamp of the template.
         */
        private final long timestamp;

        /**
         * Initialize template.
         * @param content file content
         */
        public Cache(String content) {
            this.content = content;
            timestamp = System.currentTimeMillis();
        }

        /**
         * Get the content of the template.
         */
        public String getContent() {
            return content;
        }

        /**
         * Get the creation timestamp of the template.
         */
        public long getTimestamp() {
            return timestamp;
        }
    }
}
