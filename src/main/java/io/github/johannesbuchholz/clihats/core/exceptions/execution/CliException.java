package io.github.johannesbuchholz.clihats.core.exceptions.execution;

/**
 * Base class for checked cli exceptions.
 */
public class CliException extends Exception {

    public CliException(String message) {
        super(message);
    }

    public CliException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getExitCode() {
        return 1;
    }

}
