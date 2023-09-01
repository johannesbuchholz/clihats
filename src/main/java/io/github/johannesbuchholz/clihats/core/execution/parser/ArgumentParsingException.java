package io.github.johannesbuchholz.clihats.core.execution.parser;

/**
 * Exceptions during argument parsing
 */
public class ArgumentParsingException extends Exception {

    ArgumentParsingException(String message) {
        super(message);
    }

    ArgumentParsingException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    ArgumentParsingException(String message, Throwable e) {
        super(message, e);
    }

}
