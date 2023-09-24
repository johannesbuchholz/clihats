package io.github.johannesbuchholz.clihats.core.execution.exception;

/**
 * Exception thrown during argument parsing.
 */
public class ArgumentParsingException extends Exception {

    public ArgumentParsingException(String message) {
        super(message);
    }

    public ArgumentParsingException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public ArgumentParsingException(String message, Throwable cause) {
        super(message, cause);
    }

}
