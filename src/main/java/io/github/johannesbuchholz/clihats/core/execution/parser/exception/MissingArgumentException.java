package io.github.johannesbuchholz.clihats.core.execution.parser.exception;

import io.github.johannesbuchholz.clihats.core.execution.AbstractArgumentParser;
import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;

/**
 * Thrown when a parser could not find its associated required argument.
 */
public class MissingArgumentException extends ArgumentParsingException {

    public MissingArgumentException(AbstractArgumentParser<?> failingParser) {
        super("Missing required argument "+ failingParser);
    }

}
