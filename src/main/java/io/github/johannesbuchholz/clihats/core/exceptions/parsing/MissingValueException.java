package io.github.johannesbuchholz.clihats.core.exceptions.parsing;

import io.github.johannesbuchholz.clihats.core.execution.parser.AbstractOptionParser;

/**
 * Indicates a parser found its argument but could not find any value.
 */
public class MissingValueException extends ValueExtractionException {

    public MissingValueException(AbstractOptionParser failingParser) {
        super(String.format("Missing value for option %s: No value provided or multiple valued options within a single argument.", failingParser.getDisplayName()));
    }

}
