package net.voidhttp.util.console;

import java.time.LocalDateTime;

public class Logger {
    public static void info(String message) {
        print("INFO", ConsoleFormat.WHITE, message);
    }

    public static void debug(Object message) {
        print("DEBUG", ConsoleFormat.BLUE, message);
    }

    public static void success(Object message) {
        print("SUCCESS", ConsoleFormat.GREEN, message);
    }

    public static void warn(Object message) {
        print("WARN", ConsoleFormat.YELLOW, message);
    }

    public static void error(Object message) {
        print("ERROR", ConsoleFormat.RED, message);
    }

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
