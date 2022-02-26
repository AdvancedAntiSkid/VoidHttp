package net.voidhttp.util;

import java.io.PrintStream;
import java.time.LocalDateTime;

/**
 * Represents a java print-stream override that adds timestamps for the messages.
 */
public class CustomPrintStream extends PrintStream {
    /**
     * The prefix shown before messages.
     */
    private final String prefix;

    /**
     * Initialize print stream.
     * @param prefix message prefix
     */
    public CustomPrintStream(String prefix) {
        super(System.out);
        this.prefix = prefix;
    }

    /**
     * Prints a String and then terminate the line.  This method behaves as
     * though it invokes {@link #print(String)} and then
     * {@link #println()}.
     *
     * @param x  The {@code String} to be printed.
     */
    @Override
    public void println(String x) {
        // get the current time
        LocalDateTime time = LocalDateTime.now();
        String hours = (time.getHour() < 10 ? "0" : "") + time.getHour();
        String minutes = (time.getMinute() < 10 ? "0" : "") + time.getMinute();
        String seconds = (time.getSecond() < 10 ? "0" : "") + time.getSecond();
        // print the console message
        super.printf("[%s:%s:%s] [%s/%s]: %s%n", hours, minutes, seconds, Thread.currentThread().getName(), prefix, x);
    }

    /**
     * Prints an integer and then terminate the line.  This method behaves as
     * though it invokes {@link #print(int)} and then
     * {@link #println()}.
     *
     * @param x  The {@code int} to be printed.
     */
    @Override
    public void println(int x) {
        println(String.valueOf(x));
    }

    /**
     * Prints a character and then terminate the line.  This method behaves as
     * though it invokes {@link #print(char)} and then
     * {@link #println()}.
     *
     * @param x  The {@code char} to be printed.
     */
    @Override
    public void println(char x) {
        println(String.valueOf(x));
    }

    /**
     * Prints a long and then terminate the line.  This method behaves as
     * though it invokes {@link #print(long)} and then
     * {@link #println()}.
     *
     * @param x  a The {@code long} to be printed.
     */
    @Override
    public void println(long x) {
        println(String.valueOf(x));
    }

    /**
     * Prints a float and then terminate the line.  This method behaves as
     * though it invokes {@link #print(float)} and then
     * {@link #println()}.
     *
     * @param x  The {@code float} to be printed.
     */
    @Override
    public void println(float x) {
        println(String.valueOf(x));
    }

    /**
     * Prints an array of characters and then terminate the line.  This method
     * behaves as though it invokes {@link #print(char[])} and
     * then {@link #println()}.
     *
     * @param x  an array of chars to print.
     */
    @Override
    public void println(char[] x) {
        println(String.valueOf(x));
    }

    /**
     * Prints a double and then terminate the line.  This method behaves as
     * though it invokes {@link #print(double)} and then
     * {@link #println()}.
     *
     * @param x  The {@code double} to be printed.
     */
    @Override
    public void println(double x) {
        println(String.valueOf(x));
    }

    /**
     * Prints an Object and then terminate the line.  This method calls
     * at first String.valueOf(x) to get the printed object's string value,
     * then behaves as
     * though it invokes {@link #print(String)} and then
     * {@link #println()}.
     *
     * @param x  The {@code Object} to be printed.
     */
    @Override
    public void println(Object x) {
        println(String.valueOf(x));
    }

    /**
     * Prints a boolean and then terminate the line.  This method behaves as
     * though it invokes {@link #print(boolean)} and then
     * {@link #println()}.
     *
     * @param x  The {@code boolean} to be printed
     */
    @Override
    public void println(boolean x) {
        println(String.valueOf(x));
    }

    /**
     * Terminates the current line by writing the line separator string.  The
     * line separator string is defined by the system property
     * {@code line.separator}, and is not necessarily a single newline
     * character ({@code '\n'}).
     */
    @Override
    public void println() {
        println("");
    }
}
