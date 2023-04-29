package net.voidhttp.util.threading;

import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a manager of Fusion threads.
 */
public class Threading {
    /**
     * The registry of the created threads.
     */
    private static final Map<String, ExecutorService> THREAD_REGISTRY = new MapMaker().weakValues().makeMap();

    /**
     * The increment-based thread name indicator.
     */
    private static final AtomicInteger threadId = new AtomicInteger(1);

    /**
     * The executor service creator factory.
     */
    private static final ThreadFactory FACTORY = new ThreadFactoryBuilder()
        .setNameFormat("void-thread-%d")
        .setPriority(7)
        .setUncaughtExceptionHandler(new UnhandledExceptionReporter())
        .build();

    /**
     * Create a new executor service, or retrieve the existing one, if the name is taken.
     * @param name executor name
     * @return executor with the given name
     */
    public static ExecutorService create(String name) {
        return THREAD_REGISTRY.computeIfAbsent(name, k -> Executors.newSingleThreadExecutor(FACTORY));
    }

    /**
     * Create a new executor service, or retrieve the existing one, if the name is taken.
     * The <code>$code</code> will be replaced with the next incremented identifier.
     * @param name executor name
     * @return executor with the given name
     */
    public static ExecutorService createWithId(String name) {
        return create(name.replace("$code", String.valueOf(threadId.getAndIncrement())));
    }

    /**
     * Shutdown and unregister the given executor.
     * @param name executor name
     */
    public static void terminate(String name) {
        THREAD_REGISTRY.computeIfPresent(name, (s, executor) -> {
            executor.shutdown();
            return null;
        });
    }

    /**
     * Shutdown and unregister the given executor.
     * @param executor executor service
     */
    public static void terminate(ExecutorService executor) {
        executor.shutdown();
        THREAD_REGISTRY.values().remove(executor);
    }
}
