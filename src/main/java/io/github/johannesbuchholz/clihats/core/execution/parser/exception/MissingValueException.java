package io.github.johannesbuchholz.clihats.core.execution.parser.exception;

import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;
import io.github.johannesbuchholz.clihats.core.execution.parser.AbstractOptionParser;

/**
 * Indicates a parser found its argument but could not find any value.
 */
public class MissingValueException extends ArgumentParsingException {

    public MissingValueException(AbstractOptionParser<?> failingParser) {
        super("No value provided or multiple valued options within a single argument " + failingParser.getId());
    }

}
