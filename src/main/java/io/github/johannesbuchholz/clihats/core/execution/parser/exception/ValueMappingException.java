package io.github.johannesbuchholz.clihats.core.execution.parser.exception;

import io.github.johannesbuchholz.clihats.core.execution.AbstractArgumentParser;
import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;

/**
 * Indicates a parser found its argument but could not map the extracted value.
 */
public class ValueMappingException extends ArgumentParsingException {

    public ValueMappingException(AbstractArgumentParser<?> failingParser, Throwable e) {
        super(String.format("Could not map value of %s: %s", failingParser, e.getCause()), e);
    }

}
