package io.github.johannesbuchholz.clihats.core.exceptions.parsing;

/**
 * Indicates a parser found its argument but could not find any value.
 */
public class MissingValueException extends ValueExtractionException {

    public MissingValueException(String argumentName) {
        super("Missing value for " + argumentName);
    }

}
