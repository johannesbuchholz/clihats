package io.github.johannesbuchholz.clihats.processor;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GlobalTestResult {

    private static final ByteArrayOutputStream OUT = new ByteArrayOutputStream();

    private static GlobalTestResult latestResult = null;
    private static CountDownLatch resultLatch = new CountDownLatch(1);

    public static GlobalTestResult waitForResult() {
        return waitForResult(1000);
    }

    public synchronized static GlobalTestResult waitForResult(int timeoutMs) {
        boolean isReady;
        GlobalTestResult result;
        try {
            isReady = resultLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
            result = latestResult;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            reset();
        }

        if (isReady)
            return result;
        else
            throw new RuntimeException("Timeout reached");
    }

    public static void setError(Throwable e) {
        latestResult = constructError(e);
        resultLatch.countDown();
    }

    public static void setSuccess(String commandName, Object... args) {
        latestResult = constructSuccess(commandName, args);
        resultLatch.countDown();
    }

    private synchronized static void reset() {
        OUT.reset();
        latestResult = null;
        resultLatch = new CountDownLatch(1);
    }

    private final String text;
    private final Throwable exception;
    private final Object[] args;

    public static GlobalTestResult constructSuccess(String commandName, Object... args) {
        return new GlobalTestResult(commandName, null, args);
    }

    public static GlobalTestResult constructError(Throwable e) {
        return new GlobalTestResult("Exception was thrown.", e);
    }

    private GlobalTestResult(String text, Throwable exception, Object... args) {
        this.text = text;
        this.exception = exception;
        this.args = args;
    }

    public Throwable getException() {
        return exception;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalTestResult that = (GlobalTestResult) o;
        return text.equals(that.text) && Objects.equals(exception, that.exception) && Arrays.deepEquals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(text, exception);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return "GlobalTestResult{" + "\n" +
                "text='" + text + "\n" +
                ", exception=" + exception + "\n" +
                ", args=" + Arrays.deepToString(args) + "\n" +
                '}';
    }
}
