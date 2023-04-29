package net.voidhttp.util.console;

import java.time.LocalDateTime;

/**
 * Represents a console logger that prints colorized and timestamped messages.
 */
public class Logger {
    /**
     * Print an information to the console.
     * @param message target message
     */
    public static void info(Object message) {
        print("INFO", ConsoleFormat.WHITE, message);
    }

    /**
     * Print a debug message to the console.
     * @param message target message
     */
    public static void debug(Object message) {
        print("DEBUG", ConsoleFormat.BLUE, message);
    }

    /**
     * Print a success information to the console.
     * @param message target message
     */
    public static void success(Object message) {
        print("SUCCESS", ConsoleFormat.GREEN, message);
    }

    /**
     * Print a warning to the console.
     * @param message target message
     */
    public static void warn(Object message) {
        print("WARN", ConsoleFormat.YELLOW, message);
    }

    /**
     * Print an error to the console.
     * @param message target message
     */
    public static void error(Object message) {
        print("ERROR", ConsoleFormat.RED, message);
    }

    /**
     * Print a custom message to the console.
     * @param type message type prefix
     * @param color message color
     * @param message target message
     */
    private static void print(String type, ConsoleFormat color, Object message) {
        // get the current time
        LocalDateTime time = LocalDateTime.now();
        String hours = (time.getHour() < 10 ? "0" : "") + time.getHour();
        String minutes = (time.getMinute() < 10 ? "0" : "") + time.getMinute();
        String seconds = (time.getSecond() < 10 ? "0" : "") + time.getSecond();
        // print the console message
        System.out.printf(color + "[%s:%s:%s] [%s/%s]: %s%n" + ConsoleFormat.DEFAULT,
            hours, minutes, seconds, Thread.currentThread().getName(), type, message);
    }
}
