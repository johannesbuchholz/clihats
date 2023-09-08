package io.github.johannesbuchholz.clihats.core.execution.parser;

/**
 * Indicates a parser found its argument but could not find any value.
 */
public class MissingValueException extends ArgumentParsingException {

    MissingValueException(AbstractOptionParser<?> failingParser) {
        super("No value provided or multiple valued options within a single argument " + failingParser.getId());
    }

}
