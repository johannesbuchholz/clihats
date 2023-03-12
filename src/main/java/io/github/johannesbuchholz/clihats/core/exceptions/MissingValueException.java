package io.github.johannesbuchholz.clihats.core.exceptions;

/**
 * Indicates a parser found its argument but could not find any value.
 */
public class MissingValueException extends Exception {

    public MissingValueException(String argumentName) {
        super("Missing value for argument " + argumentName);
    }

}
