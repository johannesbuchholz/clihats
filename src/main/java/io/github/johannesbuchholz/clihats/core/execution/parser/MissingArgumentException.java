package io.github.johannesbuchholz.clihats.core.execution.parser;

/**
 * Thrown when a parser could not find its associated required argument.
 */
public class MissingArgumentException extends ArgumentParsingException {

    MissingArgumentException(AbstractParser<?> failingParser) {
        super("Missing required argument "+ failingParser.getId());
    }

}
