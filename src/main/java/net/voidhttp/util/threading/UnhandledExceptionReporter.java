package net.voidhttp.util.threading;

/**
 * Represents a thread uncaught exception reporter.
 */
public class UnhandledExceptionReporter implements Thread.UncaughtExceptionHandler {
    /**
     * Handle uncaught thread exception.
     * @param thread target thread
     * @param e unhandled exception
     */
    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        System.err.println("An uncaught exception occurred on thread " + thread.getName() + "\n"
            + "\tpriority: " + thread.getPriority() + "\n"
            + "\tstate: " + thread.getState() + "\n"
            + "\tid: " + thread.getId()
        );
        e.printStackTrace();
    }
}
